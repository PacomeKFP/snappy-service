package inc.yowyob.service.snappy.domain.usecases.organization;

import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DeleteOrganizationUseCase implements MonoUseCase<String, Void> {

  private final OrganizationRepository organizationRepository;

  public DeleteOrganizationUseCase(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Override
  public Mono<Void> execute(String organizationId) {
    // Convert ID to UUID
    UUID uuid;
    try {
      uuid = UUID.fromString(organizationId);
    } catch (IllegalArgumentException e) {
      return Mono.error(new IllegalArgumentException(
          "L'identifiant fourni n'est pas un UUID valide : " + organizationId, e));
    }

    // Check that organization exists, then delete
    return organizationRepository.existsById(uuid)
        .flatMap(exists -> {
          if (!exists) {
            return Mono.error(new EntityNotFoundException(
                "Organisation avec l'ID " + organizationId + " introuvable."));
          }
          return organizationRepository.deleteById(uuid);
        });
  }
}
