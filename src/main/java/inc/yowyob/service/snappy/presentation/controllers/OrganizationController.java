package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.domain.usecases.organization.CreateOrganizationUseCase;
import inc.yowyob.service.snappy.domain.usecases.organization.DeleteOrganizationUseCase;
import inc.yowyob.service.snappy.domain.usecases.organization.GetAllOrganizationsUseCase;
import inc.yowyob.service.snappy.domain.usecases.organization.GetOrganizationUseCase;
import inc.yowyob.service.snappy.presentation.dto.organization.CreateOrganizationDto;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {
  private final GetOrganizationUseCase getOrganizationUseCase;
  private final DeleteOrganizationUseCase deleteOrganizationUseCase;
  private final CreateOrganizationUseCase createOrganizationUseCase;
  private final GetAllOrganizationsUseCase getAllOrganizationsUseCase;

  public OrganizationController(
      GetOrganizationUseCase getOrganizationUseCase,
      DeleteOrganizationUseCase deleteOrganizationUseCase,
      CreateOrganizationUseCase createOrganizationUseCase,
      GetAllOrganizationsUseCase getAllOrganizationsUseCase) {
    this.getOrganizationUseCase = getOrganizationUseCase;
    this.deleteOrganizationUseCase = deleteOrganizationUseCase;
    this.createOrganizationUseCase = createOrganizationUseCase;
    this.getAllOrganizationsUseCase = getAllOrganizationsUseCase;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Organization> create(
      @RequestBody @Valid CreateOrganizationDto createOrganizationDto) {
    return createOrganizationUseCase.execute(createOrganizationDto);
  }

  @GetMapping("/getAll/{key}")
  public Flux<Organization> getAllOrganizations(@PathVariable String key) {
    if (!Objects.equals(key, "password")) {
      return Flux.empty();
    }
    return getAllOrganizationsUseCase.execute(true);
  }

  @GetMapping("/{id}")
  public Mono<Organization> getOrganizationById(@PathVariable String id) {
    return getOrganizationUseCase.execute(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteOrganization(@PathVariable String id) {
    return deleteOrganizationUseCase.execute(id);
  }
}
