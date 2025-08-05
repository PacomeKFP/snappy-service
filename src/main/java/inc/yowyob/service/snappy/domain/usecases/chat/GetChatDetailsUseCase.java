package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.Message;
import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.presentation.dto.chat.GetChatDetailsDto;
import inc.yowyob.service.snappy.presentation.resources.ChatDetailsResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetChatDetailsUseCase implements MonoUseCase<GetChatDetailsDto, ChatDetailsResource> {

  private final UserRepository userRepository;
  private final MessageRepository messageRepository;

  public GetChatDetailsUseCase(UserRepository userRepository, MessageRepository messageRepository) {
    this.userRepository = userRepository;
    this.messageRepository = messageRepository;
  }

  @Override
  public Mono<ChatDetailsResource> execute(GetChatDetailsDto dto) {
    // Find the users involved in the chat
    Mono<User> userMono = userRepository
        .findByExternalIdAndProjectId(dto.getUser(), dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")));

    Mono<User> interlocutorMono = userRepository
        .findByExternalIdAndProjectId(dto.getInterlocutor(), dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Interlocutor not found")));

    return Mono.zip(userMono, interlocutorMono)
        .flatMap(tuple -> {
          User user = tuple.getT1();
          User interlocutor = tuple.getT2();
          
          // Fetch messages exchanged between the two users
          return messageRepository
              .findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
                  user.getId(), interlocutor.getId(),
                  interlocutor.getId(), user.getId())
              .collectList()
              .map(messages -> {
                // Transform messages into ChatDetailsResource objects
                ChatDetailsResource resource = new ChatDetailsResource();
                resource.setUser(interlocutor); // Set the interlocutor
                resource.setMessages(messages); // Set the message details
                return resource;
              });
        });
  }
}