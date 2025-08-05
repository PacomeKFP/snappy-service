package inc.yowyob.service.snappy.infrastructure.services;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import inc.yowyob.service.snappy.domain.entities.ChatbotAttachement;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatbotAttachementRepository;
import inc.yowyob.service.snappy.presentation.resources.ChatbotWithAttachmentsResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ChatbotEnrichmentService {

    private final ChatbotAttachementRepository chatbotAttachementRepository;

    public ChatbotEnrichmentService(ChatbotAttachementRepository chatbotAttachementRepository) {
        this.chatbotAttachementRepository = chatbotAttachementRepository;
    }

    public Mono<ChatbotWithAttachmentsResource> enrichChatbotWithAttachments(Chatbot chatbot) {
        return chatbotAttachementRepository
            .findByChatbotId(chatbot.getId())
            .collectList()
            .map(attachments -> new ChatbotWithAttachmentsResource(chatbot, attachments));
    }
}