package inc.yowyob.service.snappy.domain.usecases.organization;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetOrganizationUseCase implements MonoUseCase<String, Organization> {

  private final OrganizationRepository organizationRepository;

  public GetOrganizationUseCase(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Override
  public Mono<Organization> execute(String organizationId) {
    // Convert the organizationId into UUID to be compatible with OrganizationRepository
    UUID uuid;
    try {
      uuid = UUID.fromString(organizationId);
    } catch (IllegalArgumentException e) {
      return Mono.error(new IllegalArgumentException(
          "L'identifiant fourni n'est pas un UUID valide : " + organizationId, e));
    }

    // Fetch the organization using the repository, or throw an exception if not found
    return organizationRepository
        .findById(uuid)
        .switchIfEmpty(Mono.error(new EntityNotFoundException(
            "Organisation avec l'ID " + organizationId + " introuvable.")));
  }
}
