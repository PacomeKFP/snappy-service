package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.Message;
import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.presentation.dto.chat.GetChatDetailsDto;
import inc.yowyob.service.snappy.presentation.resources.ChatDetailsResource;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class GetChatDetailsUseCase implements UseCase<GetChatDetailsDto, ChatDetailsResource> {

  private final UserRepository userRepository;
  private final MessageRepository messageRepository;

  public GetChatDetailsUseCase(UserRepository userRepository, MessageRepository messageRepository) {
    this.userRepository = userRepository;
    this.messageRepository = messageRepository;
  }

  @Override
  public Mono<ChatDetailsResource> execute(GetChatDetailsDto dto) {
    Mono<User> userMono =
        Mono.fromCallable(
                () -> userRepository.findByExternalIdAndProjectId(dto.getUser(), dto.getProjectId()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(
                optionalUser ->
                    optionalUser
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new EntityNotFoundException("User not found"))));

    Mono<User> interlocutorMono =
        Mono.fromCallable(
                () ->
                    userRepository.findByExternalIdAndProjectId(
                        dto.getInterlocutor(), dto.getProjectId()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(
                optionalUser ->
                    optionalUser
                        .map(Mono::just)
                        .orElseGet(
                            () -> Mono.error(new EntityNotFoundException("Interlocutor not found"))));

    return Mono.zip(userMono, interlocutorMono)
        .flatMap(
            tuple -> {
              User user = tuple.getT1();
              User interlocutor = tuple.getT2();

              return Mono.fromCallable(
                      () ->
                          messageRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
                              user.getId(), interlocutor.getId(),
                              interlocutor.getId(), user.getId()))
                  .subscribeOn(Schedulers.boundedElastic())
                  .map(
                      messages -> {
                        ChatDetailsResource resource = new ChatDetailsResource();
                        // Ensure interlocutor data is clean for the resource
                        User cleanInterlocutor = new User();
                        cleanInterlocutor.setId(interlocutor.getId());
                        cleanInterlocutor.setExternalId(interlocutor.getExternalId());
                        cleanInterlocutor.setProjectId(interlocutor.getProjectId());
                        cleanInterlocutor.setLogin(interlocutor.getLogin());
                        cleanInterlocutor.setDisplayName(interlocutor.getDisplayName());
                        cleanInterlocutor.setContacts(interlocutor.getContacts());
                        // Set other necessary fields but avoid sensitive ones or circular refs like organization

                        resource.setUser(cleanInterlocutor); // Set the cleaned interlocutor
                        resource.setMessages(messages); // Set the message details
                        return resource;
                      });
            });
  }
}
