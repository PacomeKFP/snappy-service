package inc.yowyob.service.snappy.infrastructure.configs;

import inc.yowyob.service.snappy.domain.entities.Organization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
@EnableR2dbcAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class R2dbcConfig {
    
    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now());
    }

    @Bean
    BeforeConvertCallback<Organization> organizationBeforeConvertCallback() {
        return (entity, sqlIdentifier) -> {
            // When loading from database, mark as not new
            if (entity.getId() != null) {
                entity.markAsNotNew();
            }
            return Mono.just(entity);
        };
    }
}