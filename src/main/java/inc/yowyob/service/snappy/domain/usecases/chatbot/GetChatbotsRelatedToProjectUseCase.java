package inc.yowyob.service.snappy.domain.usecases.chatbot;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import inc.yowyob.service.snappy.domain.usecases.FluxUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatbotRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class GetChatbotsRelatedToProjectUseCase implements FluxUseCase<String, Chatbot> {

  private final ChatbotRepository chatbotRepository;

  public GetChatbotsRelatedToProjectUseCase(ChatbotRepository chatbotRepository) {
    this.chatbotRepository = chatbotRepository;
  }

  @Override
  public Flux<Chatbot> execute(String projectId) {
    if (projectId == null || projectId.isEmpty()) {
      return chatbotRepository.findAll();
    }
    return chatbotRepository.findChatbotByProjectId(projectId);
  }
}