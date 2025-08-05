package inc.yowyob.service.snappy.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import inc.yowyob.service.snappy.infrastructure.helpers.LocalDateTimeDeserializer;
import inc.yowyob.service.snappy.infrastructure.helpers.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("chatbots")
public class Chatbot implements Persistable<UUID> {

  @Id
  private UUID id;

  private String label;
  private String prompt;
  private String description;

  @Column("project_id")
  private String projectId;

  // Store enum as string for R2DBC compatibility
  @Column("language_model")
  private String languageModel; // was ChatbotLLM enum

  @Column("access_key")
  private String accessKey;

  // Note: R2DBC doesn't support @OneToMany relationships directly
  // Relationships will be handled through separate repository queries
  // private List<ChatbotAttachement> chatbotAttachements;

  @Column("organization_id")
  private UUID organizationId; // Reference to organization

  @CreatedDate
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @Column("created_at")
  private LocalDateTime createdAt;

  @LastModifiedDate
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @Column("updated_at")
  private LocalDateTime updatedAt;

  // Track if this is a new entity for R2DBC
  @JsonIgnore
  @Transient  // Exclude from R2DBC mapping
  private boolean isNew = true;

  // Constructor for creating new chatbots
  public Chatbot(String label, String prompt, String description, String projectId, UUID organizationId) {
    this.id = UUID.randomUUID();
    this.label = label;
    this.prompt = prompt;
    this.description = description;
    this.projectId = projectId;
    this.organizationId = organizationId;
    this.isNew = true;
  }

  @Override
  @JsonIgnore
  public boolean isNew() {
    // An entity is new if ID is null OR if explicitly marked as new
    return this.id == null || this.isNew;
  }

  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  // Override setId to mark as not new when ID is set from database
  public void setId(UUID id) {
    this.id = id;
    if (id != null) {
      this.isNew = false;
    }
  }
}