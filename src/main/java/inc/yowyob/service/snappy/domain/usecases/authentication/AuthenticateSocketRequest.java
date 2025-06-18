package inc.yowyob.service.snappy.domain.usecases.authentication;

import com.corundumstudio.socketio.HandshakeData;
import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.exceptions.AuthenticationFailedException;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.infrastructure.services.JwtService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class AuthenticateSocketRequest implements UseCase<HandshakeData, User> {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  public AuthenticateSocketRequest(JwtService jwtService, UserRepository userRepository) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
  }

  @Override
  public Mono<User> execute(HandshakeData handshakeData) {
    String userExternalId = handshakeData.getSingleUrlParam("user");
    String projectId = handshakeData.getSingleUrlParam("projectId");

    // extraire les claims du token jwt, et retourner les infos de l'user

    return Mono.fromCallable(
            () -> userRepository.findByExternalIdAndProjectId(userExternalId, projectId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            optionalUser ->
                optionalUser
                    .map(Mono::just)
                    .orElseGet(
                        () -> Mono.error(new AuthenticationFailedException("The user not found"))));
  }
}
