package inc.yowyob.service.snappy.domain.usecases.chat;

import inc.yowyob.service.snappy.domain.entities.MessageAttachement;
import inc.yowyob.service.snappy.domain.usecases.UseCase;
import inc.yowyob.service.snappy.infrastructure.configs.UploadProperties;
import inc.yowyob.service.snappy.infrastructure.repositories.MessageAttachementRepository;
import inc.yowyob.service.snappy.presentation.dto.chat.SaveMessageAttachementDto;
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
public class SaveMessageAttachementUseCase
    implements UseCase<SaveMessageAttachementDto, Flux<MessageAttachement>> {

  private final UploadProperties uploadProperties;
  private final MessageAttachementRepository messageAttachementRepository;

  public SaveMessageAttachementUseCase(
      MessageAttachementRepository messageAttachementRepository,
      UploadProperties uploadProperties) {
    this.messageAttachementRepository = messageAttachementRepository;
    this.uploadProperties = uploadProperties;
  }

  @Override
  public Flux<MessageAttachement> execute(SaveMessageAttachementDto dto) {
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

                                MessageAttachement messageAttachement = new MessageAttachement();
                                messageAttachement.setFilename(file.getOriginalFilename());
                                messageAttachement.setMimetype(file.getContentType());
                                messageAttachement.setFilesize(file.getSize());
                                messageAttachement.setPath(publicPath);
                                messageAttachement.setMessage(dto.getMessage());

                                log.info("File processed for saving: {}", uniqueFileName);
                                return messageAttachement;
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
                messageAttachements -> {
                  if (messageAttachements.isEmpty()) {
                    return Flux.empty();
                  }
                  return Mono.fromCallable(
                          () -> messageAttachementRepository.saveAll(messageAttachements))
                      .subscribeOn(Schedulers.boundedElastic())
                      .doOnSuccess(
                          savedList ->
                              log.info(
                                  "Saved {} attachements for message {}",
                                  savedList.size(),
                                  dto.getMessage().getId()))
                      .flatMapMany(Flux::fromIterable);
                }));
  }
}
