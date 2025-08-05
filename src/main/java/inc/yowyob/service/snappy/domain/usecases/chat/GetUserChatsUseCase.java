package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.Message;
import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.FluxUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.presentation.dto.chat.GetUserChatsDto;
import inc.yowyob.service.snappy.presentation.resources.ChatResource;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GetUserChatsUseCase implements FluxUseCase<GetUserChatsDto, ChatResource> {

  private final UserRepository userRepository;
  private final MessageRepository messageRepository;

  public GetUserChatsUseCase(UserRepository userRepository, MessageRepository messageRepository) {
    this.userRepository = userRepository;
    this.messageRepository = messageRepository;
  }

  @Override
  public Flux<ChatResource> execute(GetUserChatsDto dto) {
    // Step 1: Retrieve the user
    return userRepository
        .findByExternalIdAndProjectId(dto.getExternalUserId(), dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")))
        .flatMapMany(user -> {
          // Step 2: Fetch all messages where the user is either the sender or receiver
          return messageRepository
              .findBySenderIdOrReceiverId(user.getId(), user.getId())
              .collectList()
              .flatMapMany(messages -> {
                // Step 3: Group messages by the interlocutor
                Map<UUID, List<Message>> groupedByInterlocutorId = 
                    messages.stream()
                        .collect(Collectors.groupingBy(message -> 
                            message.getSenderId().equals(user.getId()) 
                                ? message.getReceiverId() 
                                : message.getSenderId()));

                // Step 4: For each interlocutor, create a ChatResource
                return Flux.fromIterable(groupedByInterlocutorId.entrySet())
                    .flatMap(entry -> {
                      UUID interlocutorId = entry.getKey();
                      List<Message> chatMessages = entry.getValue();
                      
                      Message lastMessage = chatMessages.stream()
                          .max(Comparator.comparing(Message::getCreatedAt))
                          .orElse(null);

                      // Fetch the interlocutor user details
                      return userRepository.findById(interlocutorId)
                          .map(interlocutor -> {
                            ChatResource chatResource = new ChatResource();
                            chatResource.setUser(interlocutor);
                            chatResource.setLastMessage(lastMessage);
                            return chatResource;
                          })
                          .switchIfEmpty(Mono.empty()); // Skip if interlocutor not found
                    });
              });
        });
  }
}