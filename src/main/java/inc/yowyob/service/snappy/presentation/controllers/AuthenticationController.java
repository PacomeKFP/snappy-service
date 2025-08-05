package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.authentication.AuthenticateOrganizationUseCase;
import inc.yowyob.service.snappy.domain.usecases.authentication.AuthenticateUserUseCase;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateOrganizationDto;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateUserDto;
import inc.yowyob.service.snappy.presentation.resources.AuthenticationResource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthenticationController {

  private final AuthenticateOrganizationUseCase authenticateOrganizationUseCase;
  private final AuthenticateUserUseCase authenticateUserUseCase;

  public AuthenticationController(
      AuthenticateOrganizationUseCase authenticateOrganizationUseCase,
      AuthenticateUserUseCase authenticateUserUseCase) {
    this.authenticateOrganizationUseCase = authenticateOrganizationUseCase;
    this.authenticateUserUseCase = authenticateUserUseCase;
  }

  /** Authenticate an organization and return JWT token. */
  @PostMapping("/organization")
  public Mono<AuthenticationResource<Organization>> authenticateOrganization(
      @RequestBody @Valid AuthenticateOrganizationDto dto) {
    return authenticateOrganizationUseCase.execute(dto);
  }

  /** Authenticate a user and return JWT token. */
  @PostMapping("/user")
  public Mono<AuthenticationResource<User>> authenticateUser(
      @RequestBody @Valid AuthenticateUserDto dto) {
    return authenticateUserUseCase.execute(dto);
  }
}