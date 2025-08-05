package inc.yowyob.service.snappy.domain.usecases.user;

import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.FluxUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class FindAllUsersUseCase implements FluxUseCase<String, User> {

  private final UserRepository userRepository;

  public FindAllUsersUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Flux<User> execute(String projectId) {
    // Validate projectId
    if (projectId == null || projectId.isEmpty()) {
      return Flux.error(new IllegalArgumentException("Project ID cannot be null or empty."));
    }

    // For now, return all users. We may need to add a projectId filter
    // to the UserRepository if we want to filter by project
    return userRepository.findAll();
  }
}