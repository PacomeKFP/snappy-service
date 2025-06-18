package inc.yowyob.service.snappy.domain.usecases.organization;

import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class DeleteOrganizationUseCase implements UseCase<String, Void> {

  private final OrganizationRepository organizationRepository;

  public DeleteOrganizationUseCase(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Override
  public Mono<Void> execute(String organizationId) {
    UUID uuid;
    try {
      uuid = UUID.fromString(organizationId);
    } catch (IllegalArgumentException e) {
      return Mono.error(
          new IllegalArgumentException(
              "L'identifiant fourni n'est pas un UUID valide : " + organizationId, e));
    }

    return Mono.fromCallable(() -> organizationRepository.existsById(uuid))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            exists -> {
              if (!exists) {
                return Mono.error(
                    new EntityNotFoundException(
                        "Organisation avec l'ID " + organizationId + " introuvable."));
              }
              return Mono.fromRunnable(() -> organizationRepository.deleteById(uuid))
                  .subscribeOn(Schedulers.boundedElastic())
                  .then();
            });
  }
}
