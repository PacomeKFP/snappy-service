package inc.yowyob.service.snappy.domain.usecases.authentication;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import inc.yowyob.service.snappy.infrastructure.services.JwtService;
import inc.yowyob.service.snappy.presentation.dto.authentication.AuthenticateOrganizationDto;
import inc.yowyob.service.snappy.presentation.resources.AuthenticationResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthenticateOrganizationUseCase
    implements MonoUseCase<AuthenticateOrganizationDto, AuthenticationResource<Organization>> {

  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  @Value("${jwt.secret}")
  private String jwtSecret;
  @Value("${jwt.expiration}")
  private long jwtExpirationMs;

  public AuthenticateOrganizationUseCase(
      OrganizationRepository organizationRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.organizationRepository = organizationRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Override
  public Mono<AuthenticationResource<Organization>> execute(AuthenticateOrganizationDto dto) {
    // Validation
    if (dto == null || dto.getEmail() == null || dto.getPassword() == null) {
      return Mono.error(new IllegalArgumentException("Email et mot de passe sont obligatoires !"));
    }

    return organizationRepository
        .findByEmail(dto.getEmail())
        .switchIfEmpty(
            Mono.error(new IllegalArgumentException("Aucune organisation trouvée avec cet email.")))
        .flatMap(organization -> {
          // Verify password
          if (!passwordEncoder.matches(dto.getPassword(), organization.getPassword())) {
            return Mono.error(new IllegalArgumentException("Mot de passe incorrect !"));
          }
          
          // Generate JWT token
          String token = jwtService.generateToken(organization);
          return Mono.just(new AuthenticationResource<>(organization, token));
        });
  }
}