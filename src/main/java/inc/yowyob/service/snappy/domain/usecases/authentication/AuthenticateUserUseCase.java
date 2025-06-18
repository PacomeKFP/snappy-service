package inc.yowyob.service.snappy.domain.usecases.authentication;

import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.exceptions.AuthenticationFailedException;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.infrastructure.services.JwtService;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateUserDto;
import inc.yowyob.service.snappy.presentation.resources.AuthenticationResource;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AuthenticateUserUseCase
    implements UseCase<AuthenticateUserDto, AuthenticationResource<User>> {

  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthenticateUserUseCase(
      JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Mono<AuthenticationResource<User>> execute(AuthenticateUserDto authenticateUserDto) {
    return Mono.fromCallable(
            () ->
                userRepository.findByLoginAndProjectId(
                    authenticateUserDto.getLogin(), authenticateUserDto.getProjectId()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            optionalUser ->
                optionalUser
                    .map(Mono::just)
                    .orElseGet(
                        () ->
                            Mono.error(
                                new AuthenticationFailedException(
                                    "Invalid credential provided; no user with login {"
                                        + authenticateUserDto.getLogin()
                                        + "} found for the specified project"))))
        .flatMap(
            user -> {
              if (!passwordEncoder.matches(authenticateUserDto.getSecret(), user.getSecret())) {
                return Mono.error(new AuthenticationFailedException("Mot de passe incorrect !"));
              }
              Map<String, Object> claims =
                  Map.of(
                      "userId",
                      user.getId(),
                      "externalId",
                      user.getExternalId(),
                      "projectId",
                      user.getProjectId());
              // Assuming jwtService.generateToken is non-blocking or will be handled later
              return Mono.just(
                  new AuthenticationResource<User>(user, jwtService.generateToken(claims, user)));
            });
  }
}
