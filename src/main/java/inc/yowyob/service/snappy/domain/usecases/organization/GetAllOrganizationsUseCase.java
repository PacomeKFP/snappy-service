package inc.yowyob.service.snappy.domain.usecases.organization;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.usecases.FluxUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class GetAllOrganizationsUseCase implements FluxUseCase<Boolean, Organization> {

  private final OrganizationRepository organizationRepository;

  public GetAllOrganizationsUseCase(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Override
  public Flux<Organization> execute(Boolean input) {
    return organizationRepository.findAll();
  }
}
