package inc.yowyob.service.snappy.domain.usecases.user;

import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.FluxUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.presentation.dto.user.FindUserByDisplayNameDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class FindUserByDisplayNameUseCase implements FluxUseCase<FindUserByDisplayNameDto, User> {

  private final UserRepository userRepository;

  public FindUserByDisplayNameUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Flux<User> execute(FindUserByDisplayNameDto dto) {
    return userRepository.findByDisplayNameAndProjectId(dto.getDisplayName(), dto.getProjectId());
  }
}