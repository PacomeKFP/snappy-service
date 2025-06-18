package inc.yowyob.service.snappy.domain.usecases.organization;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class GetAllOrganizationsUseCase implements UseCase<Boolean, Flux<Organization>> {

  private final OrganizationRepository organizationRepository;

  public GetAllOrganizationsUseCase(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Override
  public Flux<Organization> execute(Boolean input) {
    return Mono.fromCallable(organizationRepository::findAll)
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
  }
}
