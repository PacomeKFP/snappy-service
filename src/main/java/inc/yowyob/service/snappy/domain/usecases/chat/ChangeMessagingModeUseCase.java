package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.Chat;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.presentation.dto.chat.ChangeMessagingModeDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ChangeMessagingModeUseCase implements UseCase<ChangeMessagingModeDto, Chat> {

  private final UserRepository userRepository;
  private final ChatRepository chatRepository;

  public ChangeMessagingModeUseCase(UserRepository userRepository, ChatRepository chatRepository) {
    this.userRepository = userRepository;
    this.chatRepository = chatRepository;
  }

  @Override
  public Mono<Chat> execute(ChangeMessagingModeDto dto) {
    if (dto.getRequesterId() == null
        || dto.getRequesterId().isBlank()
        || dto.getInterlocutorId() == null
        || dto.getInterlocutorId().isBlank()
        || dto.getTargetMode() == null
        || dto.getProjectId() == null
        || dto.getProjectId().isBlank()) {
      return Mono.error(new IllegalArgumentException("Invalid input: All fields are required"));
    }

    Mono<Void> requesterExists =
        Mono.fromCallable(
                () -> userRepository.findByExternalIdAndProjectId(
                    dto.getRequesterId(), dto.getProjectId()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(
                optionalUser ->
                    optionalUser.isPresent()
                        ? Mono.empty()
                        : Mono.error(new EntityNotFoundException("Requester not found")));

    Mono<Void> interlocutorExists =
        Mono.fromCallable(
                () -> userRepository.findByExternalIdAndProjectId(
                    dto.getInterlocutorId(), dto.getProjectId()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(
                optionalUser ->
                    optionalUser.isPresent()
                        ? Mono.empty()
                        : Mono.error(new EntityNotFoundException("Interlocutor not found")));

    return requesterExists
        .then(interlocutorExists)
        .then(
            Mono.fromCallable(
                    () ->
                        chatRepository.findByProjectIdAndReceiverAndSender(
                            dto.getProjectId(), dto.getRequesterId(), dto.getInterlocutorId()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(
                    optionalChat -> {
                      Chat chatToSave;
                      if (optionalChat.isPresent()) {
                        chatToSave = optionalChat.get();
                        chatToSave.setMode(dto.getTargetMode());
                      } else {
                        chatToSave = new Chat();
                        chatToSave.setProjectId(dto.getProjectId());
                        chatToSave.setReceiver(dto.getRequesterId());
                        chatToSave.setSender(dto.getInterlocutorId());
                        chatToSave.setMode(dto.getTargetMode());
                      }
                      return Mono.fromCallable(() -> chatRepository.save(chatToSave))
                          .subscribeOn(Schedulers.boundedElastic());
                    }));
  }
}
