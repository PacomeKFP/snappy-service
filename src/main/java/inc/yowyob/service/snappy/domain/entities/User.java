package inc.yowyob.service.snappy.domain.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import inc.yowyob.service.snappy.infrastructure.helpers.LocalDateTimeDeserializer;
import inc.yowyob.service.snappy.infrastructure.helpers.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
@Table("users")
public class User implements Persistable<UUID> {

  @Id
  private UUID id;

  @Column("project_id")
  private String projectId;

  @Column("external_id")
  private String externalId;

  private String avatar;

  @Column("display_name")
  private String displayName;

  private String email;

  @Column("phone_number")
  private String phoneNumber;

  private String login;

  @JsonIgnore 
  private String secret;

  private boolean isOnline;

  @Column("organization_id")
  private UUID organizationId; // Reference to organization

  @Column("custom_json")
  private String customJson; // JSON string representation of Map<String, String>

  // Note: R2DBC doesn't support @ElementCollection and complex mappings
  // Custom JSON fields are handled as JSON strings
  // For complex relationships, separate repository calls are needed
  // private List<User> contacts;
  // private Organization organization;
  // private List<Message> sentMessages;
  // private List<Message> receivedMessages;

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

  // Constructor for creating new users
  public User(String displayName, String email, String login) {
    this.id = UUID.randomUUID();
    this.displayName = displayName;
    this.email = email;
    this.login = login;
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

  // Helper methods for custom JSON handling
  @JsonIgnore
  public Map<String, String> getCustomJsonAsMap() {
    if (customJson == null || customJson.trim().isEmpty()) {
      return new HashMap<>();
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(customJson, new TypeReference<Map<String, String>>() {});
    } catch (JsonProcessingException e) {
      return new HashMap<>();
    }
  }

  public void setCustomJsonFromMap(Map<String, String> customMap) {
    if (customMap == null) {
      this.customJson = null;
      return;
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      this.customJson = mapper.writeValueAsString(customMap);
    } catch (JsonProcessingException e) {
      this.customJson = "{}";
    }
  }
}
