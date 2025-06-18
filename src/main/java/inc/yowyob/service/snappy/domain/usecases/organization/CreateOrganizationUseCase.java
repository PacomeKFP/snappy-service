package inc.yowyob.service.snappy.domain.usecases.organization;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.exceptions.EntityAlreadyExistsException;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.domain.usecases.authentication.AuthenticateOrganizationUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateOrganizationDto;
import inc.yowyob.service.snappy.presentation.dto.organization.CreateOrganizationDto;
import inc.yowyob.service.snappy.presentation.resources.AuthenticationResource;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class CreateOrganizationUseCase
    implements UseCase<CreateOrganizationDto, AuthenticationResource<Organization>> {

  private final BCryptPasswordEncoder passwordEncoder;
  private final OrganizationRepository organizationRepository;
  private final AuthenticateOrganizationUseCase authenticateOrganizationUseCase;

  public CreateOrganizationUseCase(
      BCryptPasswordEncoder passwordEncoder,
      OrganizationRepository organizationRepository,
      AuthenticateOrganizationUseCase authenticateOrganizationUseCase) {
    this.passwordEncoder = passwordEncoder;
    this.organizationRepository = organizationRepository;
    this.authenticateOrganizationUseCase = authenticateOrganizationUseCase;
  }

  @Override
  public Mono<AuthenticationResource<Organization>> execute(
      CreateOrganizationDto createOrganizationDto) {
    // Vérification si une organisation avec l'email existe déjà
    return Mono.fromCallable(() -> organizationRepository.findAll())
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            organizations -> {
              boolean emailExists =
                  organizations.stream()
                      .anyMatch(org -> org.getEmail().equals(createOrganizationDto.getEmail()));
              if (emailExists) {
                return Mono.error(
                    new EntityAlreadyExistsException(
                        "Une organisation avec cet email existe déjà : "
                            + createOrganizationDto.getEmail()));
              }
              return Mono.just(createOrganizationDto); // Pass DTO to next step
            })
        .flatMap(
            dto -> // Hachage du mot de passe
                Mono.fromCallable(() -> passwordEncoder.encode(dto.getPassword()))
                    .subscribeOn(Schedulers.parallel()) // Password encoding is CPU intensive
                    .map(
                        hashedPassword -> {
                          // Création de l'organisation
                          Organization org =
                              new Organization(dto.getName(), dto.getEmail(), hashedPassword);
                          org.setProjectId(UUID.randomUUID().toString());
                          org.setPrivateKey(UUID.randomUUID().toString());
                          return org;
                        }))
        .flatMap(
            org -> // Sauvegarde dans la base
                Mono.fromCallable(() -> organizationRepository.save(org))
                    .subscribeOn(Schedulers.boundedElastic()))
        .flatMap(
            savedOrg -> // Authenticate
                this.authenticateOrganization(savedOrg.getEmail(), createOrganizationDto.getPassword()));
  }

  private Mono<AuthenticationResource<Organization>> authenticateOrganization(
      String email, String password) {
    AuthenticateOrganizationDto authenticateOrganizationDto = new AuthenticateOrganizationDto();
    authenticateOrganizationDto.setEmail(email);
    authenticateOrganizationDto.setPassword(password);
    // authenticateOrganizationUseCase.execute already returns Mono
    return authenticateOrganizationUseCase.execute(authenticateOrganizationDto);
  }
}
