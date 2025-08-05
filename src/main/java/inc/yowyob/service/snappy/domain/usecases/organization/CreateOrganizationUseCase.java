package inc.yowyob.service.snappy.domain.usecases.organization;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.exceptions.EntityAlreadyExistsException;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import inc.yowyob.service.snappy.presentation.dto.organization.CreateOrganizationDto;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateOrganizationUseCase
    implements MonoUseCase<CreateOrganizationDto, Organization> {

  private final OrganizationRepository organizationRepository;

  public CreateOrganizationUseCase(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Override
  public Mono<Organization> execute(CreateOrganizationDto createOrganizationDto) {
    // Check if organization with email already exists
    return organizationRepository.findByEmail(createOrganizationDto.getEmail())
        .hasElement()
        .flatMap(exists -> {
          if (exists) {
            return Mono.error(new EntityAlreadyExistsException(
                "Une organisation avec cet email existe déjà : " + createOrganizationDto.getEmail()));
          }
          
          // Create organization
          Organization org = new Organization(
              createOrganizationDto.getName(), 
              createOrganizationDto.getEmail(), 
              createOrganizationDto.getPassword()); // Not hashed anymore
          org.setProjectId(UUID.randomUUID().toString());
          org.setPrivateKey(UUID.randomUUID().toString());

          // Save to database
          return organizationRepository.save(org);
        });
  }
}
