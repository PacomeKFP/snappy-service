package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import inc.yowyob.service.snappy.domain.usecases.chatbot.CreateChatbotUseCase;
import inc.yowyob.service.snappy.domain.usecases.chatbot.GetAvailableLanguageModelsUseCase;
import inc.yowyob.service.snappy.domain.usecases.chatbot.GetChatbotsRelatedToProjectUseCase;
import inc.yowyob.service.snappy.presentation.dto.chatbot.CreateChatbotDto;
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

  public ChatbotController(
      CreateChatbotUseCase createChatbotUseCase,
      GetAvailableLanguageModelsUseCase getAvailableLanguageModelsUseCase,
      GetChatbotsRelatedToProjectUseCase getChatbotsRelatedToProjectUseCase) {
    this.createChatbotUseCase = createChatbotUseCase;
    this.getAvailableLanguageModelsUseCase = getAvailableLanguageModelsUseCase;
    this.getChatbotsRelatedToProjectUseCase = getChatbotsRelatedToProjectUseCase;
  }

  @GetMapping
  public Flux<Chatbot> getAllChatbots() {
    return getChatbotsRelatedToProjectUseCase.execute(null);
  }

  @GetMapping("/project-chatbot/{projectId}")
  public Flux<Chatbot> getChatbotsRelatedToProject(@PathVariable String projectId) {
    return getChatbotsRelatedToProjectUseCase.execute(projectId);
  }

  @GetMapping("/available-models")
  public Mono<List<String>> getAvailableLanguageModels() {
    return getAvailableLanguageModelsUseCase.execute(null).collectList();
  }

  @PostMapping
  public Mono<Chatbot> createNewChatbot(@Valid @ModelAttribute CreateChatbotDto dto) {
    return createChatbotUseCase.execute(dto);
  }
}