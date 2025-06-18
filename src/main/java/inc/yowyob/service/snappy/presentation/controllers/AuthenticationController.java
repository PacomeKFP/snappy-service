package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.authentication.AuthenticateOrganizationUseCase;
import inc.yowyob.service.snappy.domain.usecases.authentication.AuthenticateUserUseCase;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateOrganizationDto;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateUserDto;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateOrganizationDto;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateUserDto;
import inc.yowyob.service.snappy.presentation.resources.AuthenticationResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

  private final AuthenticateOrganizationUseCase authenticateOrganizationUseCase;
  private final AuthenticateUserUseCase authenticateUserUseCase;

  public AuthenticationController(
      AuthenticateOrganizationUseCase authenticateOrganizationUseCase,
      AuthenticateUserUseCase authenticateUserUseCase) {
    this.authenticateOrganizationUseCase = authenticateOrganizationUseCase;
    this.authenticateUserUseCase = authenticateUserUseCase;
  }

  @PostMapping("/organization")
  public Mono<ResponseEntity<AuthenticationResource<Organization>>> authenticateOrganization(
      @RequestBody @Validated Mono<AuthenticateOrganizationDto> dtoMono) {
    return dtoMono
        .flatMap(authenticateOrganizationUseCase::execute) // Assuming execute now returns Mono<AuthenticationResource<Organization>>
        .map(ResponseEntity::ok);
  }

  @PostMapping("/user")
  public Mono<ResponseEntity<AuthenticationResource<User>>> authenticateUser(
      @RequestBody @Validated Mono<AuthenticateUserDto> dtoMono) {
    return dtoMono
        .flatMap(authenticateUserUseCase::execute) // Assuming execute now returns Mono<AuthenticationResource<User>>
        .map(ResponseEntity::ok);
  }
}
