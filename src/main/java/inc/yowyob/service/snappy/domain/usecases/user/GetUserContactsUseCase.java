package inc.yowyob.service.snappy.domain.usecases.user;

import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.FluxUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserContactRepository;
import inc.yowyob.service.snappy.presentation.dto.user.GetUserContactsDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GetUserContactsUseCase implements FluxUseCase<GetUserContactsDto, User> {

  private final UserRepository userRepository;
  private final UserContactRepository userContactRepository;

  public GetUserContactsUseCase(UserRepository userRepository, UserContactRepository userContactRepository) {
    this.userRepository = userRepository;
    this.userContactRepository = userContactRepository;
  }

  @Override
  public Flux<User> execute(GetUserContactsDto dto) {
    // Validate input data
    if (dto.getUserExternalId() == null || dto.getUserExternalId().isBlank()) {
      return Flux.error(new IllegalArgumentException("User external ID cannot be blank or null."));
    }
    if (dto.getProjectId() == null || dto.getProjectId().isBlank()) {
      return Flux.error(new IllegalArgumentException("Project ID cannot be blank or null."));
    }

    // Find the user by external ID and verify they exist
    return userRepository
        .findByExternalIdAndProjectId(dto.getUserExternalId(), dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException(
            "User with external ID " + dto.getUserExternalId() + " not found.")))
        .flatMapMany(user -> 
            // Get contact IDs from user_contacts table
            userContactRepository.findContactIdsByUserId(user.getId())
                // For each contact ID, fetch the actual User object
                .flatMap(contactId -> userRepository.findById(contactId))
        );
  }
}