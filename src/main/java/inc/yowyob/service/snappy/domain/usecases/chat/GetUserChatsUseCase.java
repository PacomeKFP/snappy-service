package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.Message;
import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.presentation.dto.chat.GetUserChatsDto;
import inc.yowyob.service.snappy.presentation.resources.ChatResource;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class GetUserChatsUseCase implements UseCase<GetUserChatsDto, Flux<ChatResource>> {

  private final UserRepository userRepository;
  private final MessageRepository messageRepository;

  public GetUserChatsUseCase(UserRepository userRepository, MessageRepository messageRepository) {
    this.userRepository = userRepository;
    this.messageRepository = messageRepository;
  }

  @Override
  public Flux<ChatResource> execute(GetUserChatsDto dto) {
    // Step 1: Retrieve the user
    return Mono.fromCallable(
            () ->
                userRepository.findByExternalIdAndProjectId(
                    dto.getExternalUserId(), dto.getProjectId()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            optionalUser ->
                optionalUser
                    .map(Mono::just)
                    .orElseGet(() -> Mono.error(new EntityNotFoundException("User not found"))))
        .flatMapMany(
            user ->
                // Step 2: Fetch all messages where the user is either the sender or receiver
                Mono.fromCallable(
                        () -> messageRepository.findBySenderIdOrReceiverId(user.getId(), user.getId()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMapIterable(
                        messages -> {
                          // Step 3: Group messages by the interlocutor
                          Map<User, List<Message>> groupedByInterlocutor =
                              messages.stream()
                                  .collect(
                                      Collectors.groupingBy(
                                          message -> {
                                            // Ensure users are loaded for comparison if they are lazy
                                            User sender = message.getSender();
                                            // User receiver = message.getReceiver(); // Not strictly needed here based on logic
                                            return sender.equals(user)
                                                ? message.getReceiver()
                                                : sender;
                                          }));

                          // Step 4: Map grouped messages into ChatResource
                          return groupedByInterlocutor.entrySet().stream()
                              .map(
                                  entry -> {
                                    User interlocutor = entry.getKey(); // The other user in the chat
                                    List<Message> chatMessages = entry.getValue();
                                    Message lastMessage =
                                        chatMessages.stream()
                                            .max(Comparator.comparing(Message::getCreatedAt))
                                            .orElse(null); // Find the most recent message

                                    ChatResource chatResource = new ChatResource();
                                    // Ensure interlocutor data is clean for the resource
                                    User cleanInterlocutor = new User();
                                    cleanInterlocutor.setId(interlocutor.getId());
                                    cleanInterlocutor.setExternalId(interlocutor.getExternalId());
                                    cleanInterlocutor.setProjectId(interlocutor.getProjectId());
                                    cleanInterlocutor.setLogin(interlocutor.getLogin());
                                    cleanInterlocutor.setDisplayName(interlocutor.getDisplayName());
                                    // Avoid sending full contact list or sensitive info of interlocutor

                                    chatResource.setUser(cleanInterlocutor); // Set the interlocutor
                                    chatResource.setLastMessage(lastMessage); // Set the last message
                                    return chatResource;
                                  })
                              .collect(Collectors.toList());
                        }));
  }
}
