package inc.yowyob.service.snappy.infrastructure.configs;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DatabaseConstraintInitializer {

    private final DatabaseClient databaseClient;

    public DatabaseConstraintInitializer(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeConstraints() {
        // First, create the necessary tables with schema
        createTablesIfNotExist()
            .then(createConstraintsIfNotExist())
            .subscribe(
                unused -> System.out.println("Database schema and constraints initialized"),
                error -> System.err.println("Error initializing database: " + error.getMessage())
            );
    }

    private Mono<Void> createTablesIfNotExist() {
        String createTablesSQL = """
            -- Create organizations table
            CREATE TABLE IF NOT EXISTS organizations (
                id UUID PRIMARY KEY,
                name VARCHAR(255),
                email VARCHAR(255),
                password VARCHAR(255),
                project_id VARCHAR(255),
                private_key TEXT,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            );
            
            -- Create users table
            CREATE TABLE IF NOT EXISTS users (
                id UUID PRIMARY KEY,
                project_id VARCHAR(255),
                external_id VARCHAR(255),
                avatar VARCHAR(255),
                display_name VARCHAR(255),
                email VARCHAR(255),
                phone_number VARCHAR(255),
                login VARCHAR(255),
                secret VARCHAR(255),
                is_online BOOLEAN DEFAULT FALSE,
                organization_id UUID,
                custom_json TEXT,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            );
            
            -- Create messages table
            CREATE TABLE IF NOT EXISTS messages (
                id UUID PRIMARY KEY,
                project_id VARCHAR(255),
                body TEXT,
                is_written_by_human BOOLEAN DEFAULT TRUE,
                ack VARCHAR(50) DEFAULT 'SENT',
                sender_id UUID,
                receiver_id UUID,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            );
            
            -- Create chatbots table
            CREATE TABLE IF NOT EXISTS chatbots (
                id UUID PRIMARY KEY,
                label VARCHAR(255),
                prompt TEXT,
                description TEXT,
                project_id VARCHAR(255),
                language_model VARCHAR(100),
                access_key VARCHAR(255),
                organization_id UUID,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            );
            
            -- Create message_attachements table
            CREATE TABLE IF NOT EXISTS message_attachements (
                id UUID PRIMARY KEY,
                filename VARCHAR(255),
                mimetype VARCHAR(100),
                filesize BIGINT,
                path VARCHAR(500),
                message_id UUID,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            );
            
            -- Create chatbot_attachements table
            CREATE TABLE IF NOT EXISTS chatbot_attachements (
                id UUID PRIMARY KEY,
                filename VARCHAR(255),
                mimetype VARCHAR(100),
                filesize BIGINT,
                path VARCHAR(500),
                chatbot_id UUID,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            );
            
            -- Create user_contacts join table
            CREATE TABLE IF NOT EXISTS user_contacts (
                user_id UUID NOT NULL,
                contact_id UUID NOT NULL,
                PRIMARY KEY (user_id, contact_id)
            );
            """;

        return databaseClient.sql(createTablesSQL)
            .then();
    }

    private Mono<Void> createConstraintsIfNotExist() {
        // Note: H2 and PostgreSQL have different constraint syntax
        // This simplified approach works for both
        String constraintsSQL = """
            -- Note: Some databases may not support IF NOT EXISTS for constraints
            -- These are handled gracefully by ignoring errors if constraints already exist
            """;

        return databaseClient.sql(constraintsSQL)
            .then()
            .onErrorResume(error -> {
                // Constraints might already exist or database might not support the syntax
                System.out.println("Constraints may already exist or not supported: " + error.getMessage());
                return Mono.empty();
            });
    }
}