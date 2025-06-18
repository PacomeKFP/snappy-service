package inc.yowyob.service.snappy.domain.usecases.chatbot;

import inc.yowyob.service.snappy.domain.entities.Chatbot;
import inc.yowyob.service.snappy.domain.entities.ChatbotAttachement;
import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatbotRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import inc.yowyob.service.snappy.presentation.dto.chatbot.CreateChatbotDto;
import inc.yowyob.service.snappy.presentation.dto.chatbot.SaveChatbotAttachementDto;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Log4j2
@Service
public class CreateChatbotUseCase implements UseCase<CreateChatbotDto, Chatbot> {

  private final ChatbotRepository chatbotRepository;
  private final OrganizationRepository organizationRepository;
  private final SaveChatbotAttachementUseCase saveChatbotAttachementUseCase;

  public CreateChatbotUseCase(
      ChatbotRepository chatbotRepository,
      OrganizationRepository organizationRepository,
      SaveChatbotAttachementUseCase saveChatbotAttachementUseCase) {
    this.chatbotRepository = chatbotRepository;
    this.organizationRepository = organizationRepository;
    this.saveChatbotAttachementUseCase = saveChatbotAttachementUseCase;
  }

  @Override
  public Mono<Chatbot> execute(CreateChatbotDto dto) {
    return Mono.fromCallable(() -> organizationRepository.findByProjectId(dto.getProjectId()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            optionalOrg ->
                optionalOrg
                    .map(Mono::just)
                    .orElseGet(
                        () -> Mono.error(new EntityNotFoundException("Project not found"))))
        .flatMap(organization -> persistChatbot(dto, organization))
        .flatMap(
            chatbot ->
                saveChatbotAttachements(dto, chatbot)
                    .thenReturn(chatbot)) // After attachments are set, return chatbot
        .doOnSuccess(this::sendChatbotDetailToAlan);
  }

  private Mono<Chatbot> persistChatbot(CreateChatbotDto dto, Organization organization) {
    Chatbot chatbot = new Chatbot();
    chatbot.setLabel(dto.getLabel());
    chatbot.setPrompt(dto.getPrompt());
    chatbot.setOrganization(organization);
    chatbot.setProjectId(dto.getProjectId());
    chatbot.setDescription(dto.getDescription());
    chatbot.setLanguageModel(dto.getLanguageModel());
    chatbot.setAccessKey(UUID.randomUUID().toString());
    return Mono.fromCallable(() -> chatbotRepository.save(chatbot))
        .subscribeOn(Schedulers.boundedElastic());
  }

  private Mono<Void> saveChatbotAttachements(CreateChatbotDto dto, Chatbot chatbot) {
    if (dto.getAttachements() == null || dto.getAttachements().isEmpty()) {
      return Mono.empty();
    }
    // saveChatbotAttachementUseCase is assumed to be reactive, returning Flux<ChatbotAttachement>
    return saveChatbotAttachementUseCase
        .execute(new SaveChatbotAttachementDto(chatbot, dto.getAttachements()))
        .collectList()
        .doOnSuccess(chatbot::setChatbotAttachements)
        .then();
  }

  private void sendChatbotDetailToAlan(Chatbot chatbot) {
    // This is a side effect, can be enhanced if it involves I/O
    log.warn(
        "Sending chatbot detail to alan -- Unimplemented. Chatbot ID: {}", chatbot.getId());
  }
}
