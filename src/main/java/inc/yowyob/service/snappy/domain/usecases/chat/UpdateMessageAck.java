package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.Message;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.domain.exceptions.IllegalStateTransitionException;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageRepository;
import inc.yowyob.service.snappy.presentation.dto.chat.UpdateMessageAckDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UpdateMessageAck implements UseCase<UpdateMessageAckDto, Message> {

  private final MessageRepository messageRepository;

  public UpdateMessageAck(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  @Override
  public Mono<Message> execute(UpdateMessageAckDto dto) {
    return Mono.fromCallable(() -> messageRepository.findById(dto.getMessageId()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            optionalMessage -> {
              if (optionalMessage.isEmpty()) {
                return Mono.error(
                    new EntityNotFoundException(
                        "Target message not found; We are unable to change his ack"));
              }
              Message message = optionalMessage.get();
              if (message.getAck().ordinal() + 1 == dto.getNewAck().ordinal()) {
                message.setAck(dto.getNewAck());
                return Mono.fromCallable(() -> messageRepository.save(message))
                    .subscribeOn(Schedulers.boundedElastic());
              } else {
                return Mono.error(
                    new IllegalStateTransitionException(
                        "Vous ne pouvez pas changer un ack de "
                            + message.getAck()
                            + " à "
                            + dto.getNewAck()));
              }
            });
  }
}
