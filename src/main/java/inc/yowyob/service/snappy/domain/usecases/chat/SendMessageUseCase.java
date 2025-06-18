package inc.yowyob.service.snappy.domain.usecases.chat;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import inc.yowyob.service.snappy.domain.callbacks.SendMessageCallback;
import inc.yowyob.service.snappy.domain.entities.*;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.helpers.WebSocketHelper;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.infrastructure.storages.ConnectedUserStorage;
import inc.yowyob.service.snappy.infrastructure.storages.NotSentMessagesStorage;
import inc.yowyob.service.snappy.presentation.dto.chat.SaveMessageAttachementDto;
import inc.yowyob.service.snappy.presentation.dto.chat.SendMessageDto;
import java.util.*;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate; // Consider WebClient for reactive
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Log4j2
public class SendMessageUseCase implements UseCase<SendMessageDto, Message> {

  private final UserRepository userRepository;
  private final ChatRepository chatRepository;
  private final SocketIOServer socketIOServer;
  private final MessageRepository messageRepository;
  private final ConnectedUserStorage connectedUserStorage;
  private final NotSentMessagesStorage notSentMessagesStorage;
  private final SaveMessageAttachementUseCase saveMessageAttachementUseCase;
  @Value("${alan.baseurl}")
  private String alanBaseUrl;
  @Value("${alan.endpoint.send-message}")
  private String alanEndpointSendMessage;

  public SendMessageUseCase(
      UserRepository userRepository,
      ChatRepository chatRepository,
      SocketIOServer socketIOServer,
      MessageRepository messageRepository,
      ConnectedUserStorage connectedUserStorage,
      NotSentMessagesStorage notSentMessagesStorage,
      SaveMessageAttachementUseCase saveMessageAttachementUseCase) {
    this.userRepository = userRepository;
    this.chatRepository = chatRepository;
    this.socketIOServer = socketIOServer;
    this.messageRepository = messageRepository;
    this.connectedUserStorage = connectedUserStorage;
    this.notSentMessagesStorage = notSentMessagesStorage;
    this.saveMessageAttachementUseCase = saveMessageAttachementUseCase;
  }

  @Override
  public Mono<Message> execute(SendMessageDto dto) {
    log.info("Starting message sending process for project: {}", dto.getProjectId());

    Mono<User> senderMono =
        Mono.fromCallable(
                () -> userRepository.findByExternalIdAndProjectId(
                    dto.getSenderId(), dto.getProjectId()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(
                optionalUser ->
                    optionalUser
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new EntityNotFoundException("Sender not found"))))
            .doOnSuccess(user -> log.debug("Sender found: {}", user.getId()));

    Mono<User> receiverMono =
        Mono.fromCallable(
                () -> userRepository.findByExternalIdAndProjectId(
                    dto.getReceiverId(), dto.getProjectId()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(
                optionalUser ->
                    optionalUser
                        .map(Mono::just)
                        .orElseGet(
                            () -> Mono.error(new EntityNotFoundException("Receiver not found"))))
            .doOnSuccess(user -> log.debug("Receiver found: {}", user.getId()));

    return Mono.zip(senderMono, receiverMono)
        .flatMap(
            tupleUsers -> {
              User sender = tupleUsers.getT1();
              User receiver = tupleUsers.getT2();
              return persistMessage(dto, sender, receiver);
            })
        .flatMap(
            message ->
                saveMessageAttachements(dto, message)
                    .thenReturn(message)) // ensure attachments are processed, then return message
        .doOnSuccess(
            message -> {
              // These are side effects, run them after message is successfully processed & saved.
              // Consider making these fully reactive if they involve I/O.
              sendMessageToReceiver(message)
                  .subscribeOn(Schedulers.boundedElastic())
                  .subscribe(); // Subscribe to trigger
              sendMessageToSender(message).subscribeOn(Schedulers.boundedElastic()).subscribe();
              sendMessageToAlan(message).subscribeOn(Schedulers.boundedElastic()).subscribe();
            })
        .doOnSuccess(
            message -> log.info("Message sending process completed for message id: {}", message.getId()));
  }

  private Mono<Void> sendMessageToAlan(Message message) {
    return Mono.fromCallable(
            () ->
                chatRepository.findByProjectIdAndReceiverAndSender(
                    message.getProjectId(),
                    message.getReceiver().getExternalId(),
                    message.getSender().getExternalId()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            optionalChat -> {
              if (optionalChat.isEmpty()) return Mono.empty();
              MessagingMode receiversMessagingMode = optionalChat.get().getMode();
              if (receiversMessagingMode == MessagingMode.OFF) return Mono.empty();

              int mode = (receiversMessagingMode == MessagingMode.ON) ? 1 : 0;
              String url = alanBaseUrl + alanEndpointSendMessage + mode;
              log.info("Sending message to Alan. URL: {}", url); // corrected logging

              // Consider using WebClient for non-blocking HTTP calls
              return Mono.fromCallable(
                      () -> {
                        RestTemplate restTemplate = new RestTemplate();
                        Object response = restTemplate.postForObject(url, message, Object.class);
                        log.info("Response from Alan: {}", response);
                        return response; // response itself is not used further, so can be Void
                      })
                  .subscribeOn(Schedulers.boundedElastic())
                  .then();
            });
  }

  private Mono<Message> persistMessage(SendMessageDto dto, User sender, User receiver) {
    Message message = new Message();
    message.setSender(sender);
    message.setReceiver(receiver);
    message.setBody(dto.getBody());
    message.setProjectId(dto.getProjectId());

    return Mono.fromCallable(() -> messageRepository.save(message))
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSuccess(savedMsg -> log.info("Message saved with id: {}", savedMsg.getId()));
  }

  private Mono<Void> saveMessageAttachements(SendMessageDto dto, Message message) {
    if (dto.getAttachements() == null || dto.getAttachements().isEmpty()) {
      return Mono.empty();
    }
    // saveMessageAttachementUseCase now returns Flux<MessageAttachement>
    return saveMessageAttachementUseCase
        .execute(new SaveMessageAttachementDto(message, dto.getAttachements()))
        .collectList()
        .doOnSuccess(message::setMessageAttachements)
        .then();
  }

  public Mono<Void> sendMessageToReceiver(@NotNull Message message) {
    return Mono.fromRunnable(
            () -> {
              String receiverId = message.getReceiver().getId().toString();
              String receiverSession = connectedUserStorage.getConnectedUserSessionId(receiverId);

              if (receiverSession != null) {
                log.warn("Receiver is connected. Session: {}", receiverSession);
                SocketIOClient client = socketIOServer.getClient(UUID.fromString(receiverSession));
                if (client != null) {
                  client.sendEvent(
                      WebSocketHelper.OutputEndpoints.SEND_MESSAGE_TO_USER,
                      new SendMessageCallback(),
                      message);
                  log.info("Message sent to receiver. UserId: {}", receiverId);
                } else {
                  log.warn("SocketIOClient not found for session: {}", receiverSession);
                  notSentMessagesStorage.addNotSentMessageForUser(receiverId, message);
                }
              } else {
                log.warn("Receiver is offline. Adding to unread messages. UserId: {}", receiverId);
                notSentMessagesStorage.addNotSentMessageForUser(receiverId, message);
              }
            })
        .subscribeOn(Schedulers.boundedElastic()); // Assuming socketIO operations might block
  }

  public Mono<Void> sendMessageToSender(@NotNull Message message) {
    return Mono.fromRunnable(
            () -> {
              String senderId = message.getSender().getId().toString();
              String senderSession = connectedUserStorage.getConnectedUserSessionId(senderId);

              if (senderSession != null) {
                log.debug("Sender is connected. Session: {}", senderSession);
                SocketIOClient client = socketIOServer.getClient(UUID.fromString(senderSession));
                if (client != null) {
                  client.sendEvent(
                      WebSocketHelper.OutputEndpoints.SEND_MESSAGE_TO_USER,
                      new SendMessageCallback(),
                      message);
                  log.info("Message sent to sender. UserId: {}", senderId);
                } else {
                  log.warn("SocketIOClient not found for session: {}", senderSession);
                }
              } else {
                log.warn("Sender is offline. UserId: {}", senderId);
              }
            })
        .subscribeOn(Schedulers.boundedElastic()); // Assuming socketIO operations might block
  }
}
