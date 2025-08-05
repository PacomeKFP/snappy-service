package inc.yowyob.service.snappy.domain.usecases.authentication;

import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.infrastructure.services.JwtService;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateUserDto;
import inc.yowyob.service.snappy.presentation.resources.AuthenticationResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AuthenticateUserUseCase
    implements MonoUseCase<AuthenticateUserDto, AuthenticationResource<User>> {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthenticateUserUseCase(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Override
  public Mono<AuthenticationResource<User>> execute(AuthenticateUserDto authenticateUserDto) {
    return userRepository
        .findByLoginAndProjectId(
            authenticateUserDto.getLogin(), authenticateUserDto.getProjectId())
        .switchIfEmpty(
            Mono.error(new IllegalArgumentException(
                "Invalid credential provided; no user with login {" 
                + authenticateUserDto.getLogin()
                + "} found for the specified project")))
        .flatMap(user -> {
          // Verify password
          if (!passwordEncoder.matches(authenticateUserDto.getSecret(), user.getSecret())) {
            return Mono.error(new IllegalArgumentException("Mot de passe incorrect !"));
          }
          
          // Create claims for JWT
          Map<String, Object> claims = Map.of(
              "userId", user.getId(),
              "externalId", user.getExternalId(),
              "projectId", user.getProjectId()
          );
          
          // Generate JWT token with claims
          String token = jwtService.generateToken(claims, user);
          return Mono.just(new AuthenticationResource<>(user, token));
        });
  }
}