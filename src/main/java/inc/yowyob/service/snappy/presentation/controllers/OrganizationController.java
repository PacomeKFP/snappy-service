package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.domain.usecases.organization.CreateOrganizationUseCase;
import inc.yowyob.service.snappy.domain.usecases.organization.DeleteOrganizationUseCase;
import inc.yowyob.service.snappy.domain.usecases.organization.GetAllOrganizationsUseCase;
import inc.yowyob.service.snappy.domain.usecases.organization.GetOrganizationUseCase;
import inc.yowyob.service.snappy.presentation.dto.organization.CreateOrganizationDto;
import inc.yowyob.service.snappy.presentation.resources.AuthenticationResource;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
// import java.util.List; // Replaced by Flux
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public Mono<ResponseEntity<AuthenticationResource<Organization>>> create(
      @RequestBody @Valid Mono<CreateOrganizationDto> createOrganizationDtoMono) {
    return createOrganizationDtoMono
        .flatMap(createOrganizationUseCase::execute) // Assuming execute returns Mono<AuthenticationResource<Organization>>
        .map(authResource -> ResponseEntity.status(HttpStatus.CREATED).body(authResource));
  }

  @GetMapping("/getAll/{key}")
  public Mono<ResponseEntity<Flux<Organization>>> getAllOrganizations(@PathVariable String key) {
    if (!Objects.equals(key, "password")) {
      return Mono.just(
          ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(Flux.empty()));
    }
    // Assuming getAllOrganizationsUseCase.execute will return Flux<Organization>
    return Mono.fromCallable(() -> getAllOrganizationsUseCase.execute(true))
        .map(ResponseEntity::ok);
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<Organization>> getOrganizationById(@PathVariable String id) {
    // Assuming getOrganizationUseCase.execute will return Mono<Organization>
    return getOrganizationUseCase
        .execute(id)
        .map(ResponseEntity::ok)
        .onErrorResume(
            EntityNotFoundException.class,
            e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
        .onErrorResume(
            IllegalArgumentException.class,
            e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
  }

  @DeleteMapping("/{id}")
  @SecurityRequirement(name = "bearerAuth")
  public Mono<ResponseEntity<Void>> deleteOrganization(@PathVariable String id) {
    // Assuming deleteOrganizationUseCase.execute will return Mono<Void>
    return deleteOrganizationUseCase
        .execute(id)
        .then(Mono.just(ResponseEntity.noContent().<Void>build()))
        .onErrorResume(
            EntityNotFoundException.class,
            e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
        .onErrorResume(
            IllegalArgumentException.class,
            e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
  }
}
