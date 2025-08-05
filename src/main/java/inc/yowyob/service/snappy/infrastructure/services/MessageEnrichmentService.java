package inc.yowyob.service.snappy.infrastructure.services;

import inc.yowyob.service.snappy.domain.entities.Message;
import inc.yowyob.service.snappy.domain.entities.MessageAttachement;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageAttachementRepository;
import inc.yowyob.service.snappy.presentation.resources.MessageWithAttachmentsResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MessageEnrichmentService {

    private final MessageAttachementRepository messageAttachementRepository;

    public MessageEnrichmentService(MessageAttachementRepository messageAttachementRepository) {
        this.messageAttachementRepository = messageAttachementRepository;
    }

    public Mono<MessageWithAttachmentsResource> enrichMessageWithAttachments(Message message) {
        return messageAttachementRepository
            .findByMessageId(message.getId())
            .collectList()
            .map(attachments -> new MessageWithAttachmentsResource(message, attachments));
    }
}