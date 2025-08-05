package inc.yowyob.service.snappy.infrastructure.repositories;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ChatbotRepository extends ReactiveCrudRepository<Chatbot, UUID> {
  Flux<Chatbot> findChatbotByProjectId(String projectId);
}
