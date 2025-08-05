package inc.yowyob.service.snappy.infrastructure.repositories;

import inc.yowyob.service.snappy.domain.entities.User;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

  Mono<User> findByExternalIdAndProjectId(String externalId, String projectId);

  Mono<User> findByLoginAndProjectId(String login, String projectId);

  Flux<User> findByDisplayNameAndProjectId(String displayName, String projectId);
}
