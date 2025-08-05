package inc.yowyob.service.snappy.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import inc.yowyob.service.snappy.infrastructure.helpers.LocalDateTimeDeserializer;
import inc.yowyob.service.snappy.infrastructure.helpers.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("organizations")
public class Organization implements Persistable<UUID>, UserDetails {

  @Id
  private UUID id;

  private String name;
  private String email;

  @JsonIgnore 
  private String password;

  @Column("project_id")
  private String projectId;

  @Column("private_key")
  private String privateKey;

  // Note: R2DBC doesn't support @OneToMany relationships
  // These would need to be managed through separate queries
  // private List<User> users;
  // private List<Chatbot> chatbots;

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

  public Organization(String name, String email, String password) {
    this.id = UUID.randomUUID(); // Generate UUID on creation
    this.name = name;
    this.email = email;
    this.password = password;
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

  // When we load from database, mark as not new
  public void markAsNotNew() {
    this.isNew = false;
  }

  // Override setId to mark as not new when ID is set from database
  public void setId(UUID id) {
    this.id = id;
    if (id != null) {
      this.isNew = false;
    }
  }

  // UserDetails implementation for Spring Security
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(); // No specific authorities for now
  }

  @Override
  public String getUsername() {
    return this.email; // Use email as username
  }

  @Override
  @JsonIgnore
  public String getPassword() {
    return this.password;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
