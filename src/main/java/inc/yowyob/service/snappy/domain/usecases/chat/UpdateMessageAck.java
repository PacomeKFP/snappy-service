package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.Message;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageRepository;
import inc.yowyob.service.snappy.presentation.dto.chat.UpdateMessageAckDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UpdateMessageAck implements MonoUseCase<UpdateMessageAckDto, Message> {

  private final MessageRepository messageRepository;

  public UpdateMessageAck(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  @Override
  public Mono<Message> execute(UpdateMessageAckDto dto) {
    return messageRepository.findById(dto.getMessageId())
        .switchIfEmpty(Mono.error(new EntityNotFoundException(
            "Target message not found; We are unable to change his ack")))
        .flatMap(message -> {
          // Update the ack status - simplified logic since we're using strings now
          message.setAck(dto.getNewAck());
          return messageRepository.save(message);
        });
  }
}