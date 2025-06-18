package inc.yowyob.service.snappy.domain.usecases.chatbot;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatbotRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class GetChatbotsRelatedToProjectUseCase implements UseCase<String, Flux<Chatbot>> {

  private final ChatbotRepository chatbotRepository;

  public GetChatbotsRelatedToProjectUseCase(ChatbotRepository chatbotRepository) {
    this.chatbotRepository = chatbotRepository;
  }

  @Override
  public Flux<Chatbot> execute(String projectId) {
    if (projectId == null || projectId.isEmpty()) {
      return Mono.fromCallable(chatbotRepository::findAll)
          .subscribeOn(Schedulers.boundedElastic())
          .flatMapMany(Flux::fromIterable);
    }
    return Mono.fromCallable(() -> chatbotRepository.findChatbotByProjectId(projectId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
  }
}
