package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.*;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.presentation.dto.chat.SendMessageDto;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SendMessageUseCase implements MonoUseCase<SendMessageDto, Message> {

  private final UserRepository userRepository;
  private final ChatRepository chatRepository;
  private final MessageRepository messageRepository;

  public SendMessageUseCase(
      UserRepository userRepository,
      ChatRepository chatRepository,
      MessageRepository messageRepository) {
    this.userRepository = userRepository;
    this.chatRepository = chatRepository;
    this.messageRepository = messageRepository;
  }

  @Override
  public Mono<Message> execute(SendMessageDto dto) {
    // Find sender and receiver
    Mono<User> senderMono = userRepository
        .findByExternalIdAndProjectId(dto.getSenderId(), dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Sender not found")));

    Mono<User> receiverMono = userRepository
        .findByExternalIdAndProjectId(dto.getReceiverId(), dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Receiver not found")));

    return Mono.zip(senderMono, receiverMono)
        .flatMap(tuple -> {
          User sender = tuple.getT1();
          User receiver = tuple.getT2();

          // Create message using constructor that generates UUID
          Message message = new Message(
              dto.getProjectId(),
              dto.getBody(),
              sender.getId(),
              receiver.getId()
          );

          return messageRepository.save(message)
              .doOnSuccess(savedMessage -> {
                // Note: WebSocket/SocketIO functionality removed as it requires authentication
                // In a real application, you might want to add event publishing here
                // to notify other services or clients about the new message
              });
        });
  }
}