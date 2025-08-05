package inc.yowyob.service.snappy.infrastructure.repositories;

import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserContactRepository {
    
    private final DatabaseClient databaseClient;
    
    public UserContactRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }
    
    public Flux<UUID> findContactIdsByUserId(UUID userId) {
        return databaseClient
            .sql("SELECT contact_id FROM user_contacts WHERE user_id = :userId")
            .bind("userId", userId)
            .map(row -> (UUID) row.get("contact_id"))
            .all();
    }
    
    public Mono<Void> addContact(UUID userId, UUID contactId) {
        return databaseClient
            .sql("INSERT INTO user_contacts (user_id, contact_id) VALUES (:userId, :contactId)")
            .bind("userId", userId) 
            .bind("contactId", contactId)
            .then();
    }
    
    public Mono<Void> removeContact(UUID userId, UUID contactId) {
        return databaseClient
            .sql("DELETE FROM user_contacts WHERE user_id = :userId AND contact_id = :contactId")
            .bind("userId", userId)
            .bind("contactId", contactId)
            .then();
    }
}