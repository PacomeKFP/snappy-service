package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import inc.yowyob.service.snappy.domain.usecases.chatbot.CreateChatbotUseCase;
import inc.yowyob.service.snappy.domain.usecases.chatbot.GetAvailableLanguageModelsUseCase;
import inc.yowyob.service.snappy.domain.usecases.chatbot.GetChatbotsRelatedToProjectUseCase;
import inc.yowyob.service.snappy.infrastructure.services.ChatbotEnrichmentService;
import inc.yowyob.service.snappy.presentation.dto.chatbot.CreateChatbotDto;
import inc.yowyob.service.snappy.presentation.resources.ChatbotWithAttachmentsResource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

  private final CreateChatbotUseCase createChatbotUseCase;
  private final GetAvailableLanguageModelsUseCase getAvailableLanguageModelsUseCase;
  private final GetChatbotsRelatedToProjectUseCase getChatbotsRelatedToProjectUseCase;
  private final ChatbotEnrichmentService chatbotEnrichmentService;

  public ChatbotController(
      CreateChatbotUseCase createChatbotUseCase,
      GetAvailableLanguageModelsUseCase getAvailableLanguageModelsUseCase,
      GetChatbotsRelatedToProjectUseCase getChatbotsRelatedToProjectUseCase,
      ChatbotEnrichmentService chatbotEnrichmentService) {
    this.createChatbotUseCase = createChatbotUseCase;
    this.getAvailableLanguageModelsUseCase = getAvailableLanguageModelsUseCase;
    this.getChatbotsRelatedToProjectUseCase = getChatbotsRelatedToProjectUseCase;
    this.chatbotEnrichmentService = chatbotEnrichmentService;
  }

  @GetMapping
  public Flux<ChatbotWithAttachmentsResource> getAllChatbots() {
    return getChatbotsRelatedToProjectUseCase.execute(null)
        .flatMap(chatbotEnrichmentService::enrichChatbotWithAttachments);
  }

  @GetMapping("/project-chatbot/{projectId}")
  public Flux<ChatbotWithAttachmentsResource> getChatbotsRelatedToProject(@PathVariable String projectId) {
    return getChatbotsRelatedToProjectUseCase.execute(projectId)
        .flatMap(chatbotEnrichmentService::enrichChatbotWithAttachments);
  }

  @GetMapping("/available-models")
  public Mono<List<String>> getAvailableLanguageModels() {
    return getAvailableLanguageModelsUseCase.execute(null).collectList();
  }

  @PostMapping
  public Mono<ChatbotWithAttachmentsResource> createNewChatbot(@Valid @ModelAttribute CreateChatbotDto dto) {
    return createChatbotUseCase.execute(dto)
        .flatMap(chatbotEnrichmentService::enrichChatbotWithAttachments);
  }
}