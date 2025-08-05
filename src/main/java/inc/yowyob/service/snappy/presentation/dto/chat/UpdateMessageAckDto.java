package inc.yowyob.service.snappy.presentation.dto.chat;

import java.util.UUID;
import lombok.Data;

@Data
public class UpdateMessageAckDto {
  UUID messageId;
  String newAck; // Changed from enum to String for R2DBC compatibility
}
