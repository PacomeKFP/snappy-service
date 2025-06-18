package inc.yowyob.service.snappy.domain.usecases.chatbot;

import inc.yowyob.service.snappy.domain.entities.ChatbotAttachement;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.configs.UploadProperties;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatbotAttachementRepository;
import inc.yowyob.service.snappy.presentation.dto.chatbot.SaveChatbotAttachementDto;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Log4j2
public class SaveChatbotAttachementUseCase
    implements UseCase<SaveChatbotAttachementDto, Flux<ChatbotAttachement>> {

  private final UploadProperties uploadProperties;
  private final ChatbotAttachementRepository chatbotAttachementRepository;

  public SaveChatbotAttachementUseCase(
      UploadProperties uploadProperties,
      ChatbotAttachementRepository chatbotAttachementRepository) {
    this.uploadProperties = uploadProperties;
    this.chatbotAttachementRepository = chatbotAttachementRepository;
  }

  @Override
  public Flux<ChatbotAttachement> execute(SaveChatbotAttachementDto dto) {
    String uploadDir = System.getProperty("user.dir") + "/" + uploadProperties.getDir();

    Mono<Void> createDirectoryMono =
        Mono.fromRunnable(
                () -> {
                  File directory = new File(uploadDir);
                  if (!directory.exists()) {
                    directory.mkdirs();
                  }
                })
            .subscribeOn(Schedulers.boundedElastic())
            .then();

    return createDirectoryMono.thenMany(
        Flux.fromIterable(dto.getAttachements())
            .flatMap(
                file ->
                    Mono.fromCallable(
                            () -> {
                              String uniqueFileName =
                                  UUID.randomUUID() + "_" + file.getOriginalFilename();
                              String absolutePath = uploadDir + File.separator + uniqueFileName;
                              String publicPath =
                                  uploadProperties.getBaseUrl() + "/" + uniqueFileName;

                              try (InputStream is = file.getInputStream();
                                  FileOutputStream fout = new FileOutputStream(absolutePath)) {
                                is.transferTo(fout);

                                ChatbotAttachement chatbotAttachement =
                                    new ChatbotAttachement();
                                chatbotAttachement.setFilename(file.getOriginalFilename());
                                chatbotAttachement.setMimetype(file.getContentType());
                                chatbotAttachement.setFilesize(file.getSize());
                                chatbotAttachement.setPath(publicPath);
                                chatbotAttachement.setChatbot(dto.getChatbot());

                                log.info("File processed for saving: {}", uniqueFileName);
                                return chatbotAttachement;
                              } catch (IOException e) {
                                log.error(
                                    "Error saving file: {}", file.getOriginalFilename(), e);
                                throw new RuntimeException(
                                    "Failed to save file: " + file.getOriginalFilename(), e);
                              }
                            })
                        .subscribeOn(Schedulers.boundedElastic()))
            .collectList()
            .flatMapMany(
                chatbotAttachements -> {
                  if (chatbotAttachements.isEmpty()) {
                    return Flux.empty();
                  }
                  return Mono.fromCallable(
                          () -> chatbotAttachementRepository.saveAll(chatbotAttachements))
                      .subscribeOn(Schedulers.boundedElastic())
                      .doOnSuccess(
                          savedList ->
                              log.info(
                                  "Saved {} attachements for chatbot {}",
                                  savedList.size(),
                                  dto.getChatbot().getId()))
                      .flatMapMany(Flux::fromIterable);
                }));
  }
}
