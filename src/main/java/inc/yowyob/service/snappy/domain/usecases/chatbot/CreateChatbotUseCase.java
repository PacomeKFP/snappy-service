package inc.yowyob.service.snappy.domain.usecases.chatbot;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import inc.yowyob.service.snappy.domain.usecases.MonoUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatbotRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import inc.yowyob.service.snappy.presentation.dto.chatbot.CreateChatbotDto;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class CreateChatbotUseCase implements MonoUseCase<CreateChatbotDto, Chatbot> {

  private final ChatbotRepository chatbotRepository;
  private final OrganizationRepository organizationRepository;

  public CreateChatbotUseCase(
      ChatbotRepository chatbotRepository,
      OrganizationRepository organizationRepository) {
    this.chatbotRepository = chatbotRepository;
    this.organizationRepository = organizationRepository;
  }

  @Override
  public Mono<Chatbot> execute(CreateChatbotDto dto) {
    return organizationRepository
        .findByProjectId(dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Project not found")))
        .flatMap(organization -> {
          // Use the constructor that generates a UUID
          Chatbot chatbot = new Chatbot(
              dto.getLabel(),
              dto.getPrompt(), 
              dto.getDescription(),
              dto.getProjectId(),
              organization.getId()
          );
          // Convert enum to string for R2DBC compatibility
          chatbot.setLanguageModel(dto.getLanguageModel() != null ? dto.getLanguageModel().toString() : null);
          chatbot.setAccessKey(generateAccessKey());

          return chatbotRepository.save(chatbot);
        })
        .doOnSuccess(savedChatbot -> 
            log.info("Created chatbot with ID: {}", savedChatbot.getId()));
  }

  private String generateAccessKey() {
    // Simple UUID-based access key generation
    return UUID.randomUUID().toString().replace("-", "");
  }
}