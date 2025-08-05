package inc.yowyob.service.snappy.domain.usecases.user;

import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.exceptions.ProjectNotFoundException;
import inc.yowyob.service.snappy.domain.exceptions.UserAlreadyExistsException;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.presentation.dto.user.CreateUserDto;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateUserUseCase implements MonoUseCase<CreateUserDto, User> {

  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;

  public CreateUserUseCase(
      UserRepository userRepository,
      OrganizationRepository organizationRepository) {
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
  }

  @Override
  public Mono<User> execute(CreateUserDto dto) {
    return validateInput(dto)
        .then(organizationRepository.findByProjectId(dto.getProjectId())
            .switchIfEmpty(Mono.error(new ProjectNotFoundException(
                "Project with ID '" + dto.getProjectId() + "' not found.")))
            .flatMap(organization -> {
              return userRepository.findByExternalIdAndProjectId(dto.getExternalId(), dto.getProjectId())
                  .hasElement()
                  .flatMap(exists -> {
                    if (exists) {
                      return Mono.error(new UserAlreadyExistsException(
                          "User with external ID '" + dto.getExternalId() + "' already exists."));
                    }
                    return createNewUser(dto, organization.getId());
                  });
            })
        );
  }

  private Mono<User> createNewUser(CreateUserDto dto, UUID organizationId) {
    // Use the constructor that generates a UUID
    User newUser = new User(dto.getDisplayName(), dto.getEmail(), dto.getLogin());
    newUser.setAvatar(dto.getAvatar());
    newUser.setPhoneNumber(dto.getPhoneNumber());
    newUser.setExternalId(dto.getExternalId());
    // Note: Removed password encoding since authentication was removed
    newUser.setSecret(dto.getSecret());
    newUser.setProjectId(dto.getProjectId());
    newUser.setOrganizationId(organizationId); // Set the organization reference
    // Note: R2DBC doesn't support complex JSON mapping like customJson directly
    // This would need to be handled as JSON string or separate table
    
    return userRepository.save(newUser);
  }

  private Mono<Void> validateInput(CreateUserDto dto) {
    if (dto.getExternalId() == null || dto.getExternalId().isEmpty()) {
      return Mono.error(new IllegalArgumentException("ExternalId is required."));
    }

    if (dto.getDisplayName() == null || dto.getDisplayName().isEmpty()) {
      return Mono.error(new IllegalArgumentException("DisplayName is required."));
    }

    if (dto.getLogin() == null || dto.getLogin().isEmpty()) {
      return Mono.error(new IllegalArgumentException("Login is required."));
    }

    if (dto.getSecret() == null || dto.getSecret().isEmpty()) {
      return Mono.error(new IllegalArgumentException("Secret is required."));
    }

    if (dto.getProjectId() == null || dto.getProjectId().isEmpty()) {
      return Mono.error(new IllegalArgumentException("ProjectId is required."));
    }

    if (dto.getEmail() != null
        && !dto.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
      return Mono.error(new IllegalArgumentException("Invalid email format."));
    }

    if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().matches("^\\+?[1-9][0-9]{1,14}$")) {
      return Mono.error(new IllegalArgumentException("Invalid phone number format."));
    }

    return Mono.empty();
  }
}