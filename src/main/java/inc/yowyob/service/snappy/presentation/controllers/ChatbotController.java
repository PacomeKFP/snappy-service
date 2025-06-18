package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import inc.yowyob.service.snappy.domain.usecases.chatbot.CreateChatbotUseCase;
import inc.yowyob.service.snappy.domain.usecases.chatbot.GetAvailableLanguageModelsUseCase;
import inc.yowyob.service.snappy.domain.usecases.chatbot.GetChatbotsRelatedToProjectUseCase;
import inc.yowyob.service.snappy.presentation.dto.chatbot.CreateChatbotDto;
import jakarta.validation.Valid;
// import java.util.List; // Replaced by Flux
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
  public Mono<ResponseEntity<Flux<Chatbot>>> getAllChatbots() {
    // Assuming getChatbotsRelatedToProjectUseCase.execute will return Flux<Chatbot>
    return Mono.fromCallable(() -> this.getChatbotsRelatedToProjectUseCase.execute(null))
        .map(ResponseEntity::ok);
  }

  @GetMapping("/project-chatbot/:projectId")
  public Mono<ResponseEntity<Flux<Chatbot>>> getChatbotsRelatedToProject(
      @RequestParam(required = true) String projectId) {
    // Assuming getChatbotsRelatedToProjectUseCase.execute will return Flux<Chatbot>
    return Mono.fromCallable(() -> this.getChatbotsRelatedToProjectUseCase.execute(projectId))
        .map(ResponseEntity::ok);
  }

  @GetMapping("/available-models")
  public Mono<ResponseEntity<Flux<String>>> getAvailableLanguageModels() {
    // Assuming getAvailableLanguageModelsUseCase.execute will return Flux<String>
    return Mono.fromCallable(() -> getAvailableLanguageModelsUseCase.execute(null))
        .map(ResponseEntity::ok);
  }

  @PostMapping
  public Mono<ResponseEntity<Chatbot>> createNewChatbot(
      @Valid @ModelAttribute CreateChatbotDto dto) {
    // @ModelAttribute is not directly compatible with Mono<T> for the parameter.
    // Service call is wrapped with Mono.fromCallable.
    // Assuming createChatbotUseCase.execute will return Mono<Chatbot>
    return Mono.fromCallable(() -> createChatbotUseCase.execute(dto))
        .flatMap(monoChatbot -> monoChatbot) // If execute already returns Mono
        .map(ResponseEntity::ok);
  }
}
