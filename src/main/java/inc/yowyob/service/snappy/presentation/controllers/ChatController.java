package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.entities.Chat;
import inc.yowyob.service.snappy.domain.entities.Message;
import inc.yowyob.service.snappy.domain.usecases.chat.ChangeMessagingModeUseCase;
import inc.yowyob.service.snappy.domain.usecases.chat.GetChatDetailsUseCase;
import inc.yowyob.service.snappy.domain.usecases.chat.GetUserChatsUseCase;
import inc.yowyob.service.snappy.domain.usecases.chat.SendMessageUseCase;
import inc.yowyob.service.snappy.presentation.dto.chat.ChangeMessagingModeDto;
import inc.yowyob.service.snappy.presentation.dto.chat.GetChatDetailsDto;
import inc.yowyob.service.snappy.presentation.dto.chat.GetUserChatsDto;
import inc.yowyob.service.snappy.presentation.dto.chat.SendMessageDto;
import inc.yowyob.service.snappy.presentation.dto.chat.ChangeMessagingModeDto;
import inc.yowyob.service.snappy.presentation.dto.chat.GetChatDetailsDto;
import inc.yowyob.service.snappy.presentation.dto.chat.GetUserChatsDto;
import inc.yowyob.service.snappy.presentation.dto.chat.SendMessageDto;
import inc.yowyob.service.snappy.presentation.resources.ChatDetailsResource;
import inc.yowyob.service.snappy.presentation.resources.ChatResource;
import jakarta.validation.Valid;
// import java.util.List; // Replaced by Flux
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/chat")
public class ChatController {

  private final GetUserChatsUseCase getUserChats;
  private final GetChatDetailsUseCase getChatDetails;
  private final SendMessageUseCase sendMessageUseCase;
  private final ChangeMessagingModeUseCase changeMessagingModeUseCase;

  public ChatController(
      GetUserChatsUseCase getUserChats,
      GetChatDetailsUseCase getChatDetails,
      SendMessageUseCase sendMessageUseCase,
      ChangeMessagingModeUseCase changeMessagingModeUseCase) {
    this.getUserChats = getUserChats;
    this.getChatDetails = getChatDetails;
    this.sendMessageUseCase = sendMessageUseCase;
    this.changeMessagingModeUseCase = changeMessagingModeUseCase;
  }

  /** Retrieve detailed chat between two users. */
  @PostMapping("/details")
  public Mono<ResponseEntity<ChatDetailsResource>> getChatDetails(
      @Valid @RequestBody Mono<GetChatDetailsDto> dtoMono) {
    return dtoMono
        .flatMap(getChatDetails::execute) // Assuming execute returns Mono<ChatDetailsResource>
        .map(ResponseEntity::ok);
  }

  /** Retrieve all active chats for a specific user. */
  @GetMapping("/{userId}/chats")
  public Mono<ResponseEntity<Flux<ChatResource>>> getUserChats(
      @PathVariable String userId, @RequestParam String projectId) {
    GetUserChatsDto dto = new GetUserChatsDto(userId, projectId);
    // Assuming getUserChats.execute(dto) will return Flux<ChatResource>
    // Wrap the synchronous call, then map to ResponseEntity
    return Mono.fromCallable(() -> getUserChats.execute(dto)).map(ResponseEntity::ok);
  }

  /** Send a message from one user to another. */
  @PostMapping(
      path = "/send",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public Mono<ResponseEntity<Message>> sendMessage(@Valid @ModelAttribute SendMessageDto dto) {
    // @ModelAttribute is not directly compatible with Mono<T> for the parameter.
    // Service call is wrapped with Mono.fromCallable.
    // Assuming sendMessageUseCase.execute will return Mono<Message>
    return Mono.fromCallable(() -> sendMessageUseCase.execute(dto))
        .flatMap(monoMessage -> monoMessage) // If execute already returns Mono
        .map(ResponseEntity::ok);
  }

  /** Change the messaging mode of a conversation */
  @PutMapping("/changeMode")
  public Mono<ResponseEntity<Chat>> changeMessagingMode(
      @Valid @RequestBody Mono<ChangeMessagingModeDto> dtoMono) {
    return dtoMono
        .flatMap(changeMessagingModeUseCase::execute) // Assuming execute returns Mono<Chat>
        .map(ResponseEntity::ok);
  }
}
