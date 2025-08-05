package inc.yowyob.service.snappy.infrastructure.repositories;

import inc.yowyob.service.snappy.domain.entities.MessageAttachement;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageAttachementRepository extends ReactiveCrudRepository<MessageAttachement, UUID> {
  
  @Query("SELECT * FROM message_attachements WHERE message_id = :messageId")
  Flux<MessageAttachement> findByMessageId(UUID messageId);
}
