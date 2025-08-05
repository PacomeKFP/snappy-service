package inc.yowyob.service.snappy.infrastructure.repositories;

import inc.yowyob.service.snappy.domain.entities.Message;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends ReactiveCrudRepository<Message, UUID> {

  // Custom query to fetch messages between two users
  Flux<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
      UUID senderId1, UUID receiverId1, UUID senderId2, UUID receiverId2);

  // Find all messages where the user is either the sender or receiver
  Flux<Message> findBySenderIdOrReceiverId(UUID senderId, UUID receiverId);
}
