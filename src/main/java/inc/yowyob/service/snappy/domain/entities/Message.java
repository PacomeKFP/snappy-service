package inc.yowyob.service.snappy.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import inc.yowyob.service.snappy.infrastructure.helpers.LocalDateTimeDeserializer;
import inc.yowyob.service.snappy.infrastructure.helpers.LocalDateTimeSerializer;
import java.time.LocalDateTime;
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
@Table("messages")
public class Message implements Persistable<UUID> {

  @Id
  private UUID id;

  @Column("project_id")
  private String projectId;

  private String body;
  
  @Column("is_written_by_human")
  private boolean isWrittenByHuman = true;

  // Store as string instead of enum for R2DBC compatibility
  private String ack = "SENT"; // was MessageAck enum

  // Foreign key references - R2DBC doesn't support object references
  @Column("sender_id")
  private UUID senderId;

  @Column("receiver_id")
  private UUID receiverId;

  // Note: messageAttachements would need to be managed via separate queries

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

  // Constructor for creating new messages
  public Message(String projectId, String body, UUID senderId, UUID receiverId) {
    this.id = UUID.randomUUID();
    this.projectId = projectId;
    this.body = body;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.isWrittenByHuman = true;
    this.ack = "SENT";
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

  // These would need to be populated via separate queries in R2DBC
  // For now, we'll handle these at the service layer
  @JsonProperty("sender")
  public UUID getSenderProjection() {
    return senderId;
  }

  @JsonProperty("receiver")  
  public UUID getReceiverProjection() {
    return receiverId;
  }
}
