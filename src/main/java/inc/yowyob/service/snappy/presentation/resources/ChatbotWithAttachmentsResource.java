package inc.yowyob.service.snappy.presentation.resources;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import inc.yowyob.service.snappy.domain.entities.ChatbotAttachement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotWithAttachmentsResource {
    private Chatbot chatbot;
    private List<ChatbotAttachement> attachments;
}