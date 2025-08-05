package inc.yowyob.service.snappy.presentation.resources;

import inc.yowyob.service.snappy.domain.entities.Message;
import inc.yowyob.service.snappy.domain.entities.MessageAttachement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageWithAttachmentsResource {
    private Message message;
    private List<MessageAttachement> attachments;
}