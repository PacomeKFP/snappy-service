package inc.yowyob.service.snappy.domain.usecases.user;

import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DeleteUserUseCase implements MonoUseCase<String, Void> {

  private final UserRepository userRepository;

  public DeleteUserUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Mono<Void> execute(String userId) {
    // Validate input
    UUID userUuid;
    try {
      userUuid = UUID.fromString(userId);
    } catch (IllegalArgumentException ex) {
      return Mono.error(new IllegalArgumentException("Invalid user ID format."));
    }

    // Check if the user exists and delete
    return userRepository.existsById(userUuid)
        .flatMap(exists -> {
          if (!exists) {
            return Mono.error(new IllegalArgumentException("User with ID " + userId + " does not exist."));
          }
          return userRepository.deleteById(userUuid);
        });
  }
}