package inc.yowyob.service.snappy.infrastructure.repositories;

import inc.yowyob.service.snappy.domain.entities.ChatbotAttachement;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ChatbotAttachementRepository extends ReactiveCrudRepository<ChatbotAttachement, UUID> {
  
  @Query("SELECT * FROM chatbot_attachements WHERE chatbot_id = :chatbotId")
  Flux<ChatbotAttachement> findByChatbotId(UUID chatbotId);
}
