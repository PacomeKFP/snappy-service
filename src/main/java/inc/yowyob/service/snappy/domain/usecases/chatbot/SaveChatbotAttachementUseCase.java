package inc.yowyob.service.snappy.domain.usecases.chatbot;

import inc.yowyob.service.snappy.domain.entities.ChatbotAttachement;
import inc.yowyob.service.snappy.domain.usecases.FluxUseCase;
import inc.yowyob.service.snappy.infrastructure.configs.UploadProperties;
import inc.yowyob.service.snappy.infrastructure.repositories.ChatbotAttachementRepository;
import inc.yowyob.service.snappy.presentation.dto.chatbot.SaveChatbotAttachementDto;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Log4j2
public class SaveChatbotAttachementUseCase implements FluxUseCase<SaveChatbotAttachementDto, ChatbotAttachement> {

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

    File directory = new File(uploadDir);
    if (!directory.exists()) {
      directory.mkdirs();
    }

    return Flux.fromIterable(dto.getAttachements())
        .flatMap(file -> saveFileAsync(file, uploadDir, dto))
        .doOnComplete(() -> log.info("Saved {} attachements for chatbot {}", 
            dto.getAttachements().size(), dto.getChatbot().getId()));
  }

  private Mono<ChatbotAttachement> saveFileAsync(MultipartFile file, String uploadDir, SaveChatbotAttachementDto dto) {
    return Mono.fromCallable(() -> {
      String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
      String absolutePath = uploadDir + File.separator + uniqueFileName;
      String publicPath = uploadProperties.getBaseUrl() + "/" + uniqueFileName;

      try {
        // Save physical file
        FileOutputStream fout = new FileOutputStream(absolutePath);
        file.getInputStream().transferTo(fout);
        fout.close();

        ChatbotAttachement chatbotAttachement = new ChatbotAttachement(
            file.getSize(),  // filesize
            file.getContentType(),  // mimetype
            file.getOriginalFilename(),  // filename
            publicPath,  // path
            dto.getChatbot().getId()  // chatbotId
        );

        log.info("File saved successfully: {}", uniqueFileName);
        return chatbotAttachement;
      } catch (Exception e) {
        log.error("Error saving file: {}", file.getOriginalFilename(), e);
        throw new RuntimeException("Failed to save file: " + file.getOriginalFilename(), e);
      }
    })
    .subscribeOn(Schedulers.boundedElastic()) // Use bounded elastic scheduler for I/O operations
    .flatMap(chatbotAttachementRepository::save);
  }
}