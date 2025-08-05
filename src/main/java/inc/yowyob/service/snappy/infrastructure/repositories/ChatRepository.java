package inc.yowyob.service.snappy.infrastructure.repositories;

import inc.yowyob.service.snappy.domain.entities.Chat;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ChatRepository extends ReactiveCrudRepository<Chat, UUID> {

  Mono<Chat> findByProjectIdAndReceiverAndSender(
      String projectId, String receiver, String sender);
}
