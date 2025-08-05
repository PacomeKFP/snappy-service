package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.Chat;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.presentation.dto.chat.ChangeMessagingModeDto;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ChangeMessagingModeUseCase implements MonoUseCase<ChangeMessagingModeDto, Chat> {

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

    // Validate users exist
    Mono<Void> validateUsers = userRepository
        .findByExternalIdAndProjectId(dto.getRequesterId(), dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Requester not found")))
        .then(userRepository
            .findByExternalIdAndProjectId(dto.getInterlocutorId(), dto.getProjectId())
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Interlocutor not found"))))
        .then();

    return validateUsers
        .then(chatRepository.findByProjectIdAndReceiverAndSender(
            dto.getProjectId(), dto.getRequesterId(), dto.getInterlocutorId()))
        .flatMap(existingChat -> {
          // Update existing chat
          existingChat.setMode(dto.getTargetMode().toString());
          return chatRepository.save(existingChat);
        })
        .switchIfEmpty(Mono.defer(() -> {
          // Create new chat
          Chat newChat = new Chat();
          newChat.setId(UUID.randomUUID());
          newChat.setProjectId(dto.getProjectId());
          newChat.setReceiver(dto.getRequesterId());
          newChat.setSender(dto.getInterlocutorId());
          newChat.setMode(dto.getTargetMode().toString());
          return chatRepository.save(newChat);
        }));
  }
}