package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.model.PreKeyBundle;
import inc.yowyob.service.snappy.domain.usecases.signal.GetPreKeyBundleUseCase;
import inc.yowyob.service.snappy.domain.usecases.signal.RegisterPreKeyBundleUseCase;
import inc.yowyob.service.snappy.presentation.dto.signal.RegisterPreKeyBundleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/signal")
@RequiredArgsConstructor
public class SignalController {

  private final RegisterPreKeyBundleUseCase registerPreKeyBundleUseCase;
  private final GetPreKeyBundleUseCase getPreKeyBundleUseCase;

  @PostMapping("/pre-key-bundle/{userId}")
  public Mono<ResponseEntity<Void>> registerPreKeyBundle(
      @PathVariable String userId, @RequestBody Mono<PreKeyBundle> preKeyBundleMono) {
    return preKeyBundleMono
        .flatMap(
            preKeyBundle -> {
              RegisterPreKeyBundleDto registerPreKeyBundleDto =
                  new RegisterPreKeyBundleDto(userId, preKeyBundle);
              // Assuming registerPreKeyBundleUseCase.execute now returns Mono<Void> or Mono<Something that can be mapped to Void>
              return registerPreKeyBundleUseCase.execute(registerPreKeyBundleDto);
            })
        .then(Mono.just(ResponseEntity.ok().<Void>build()));
  }

  @GetMapping("/pre-key-bundle/{userId}")
  public Mono<ResponseEntity<PreKeyBundle>> getPreKeyBundle(@PathVariable String userId) {
    // Assuming getPreKeyBundleUseCase.execute will return Mono<PreKeyBundle>
    return getPreKeyBundleUseCase.execute(userId).map(ResponseEntity::ok);
  }
}
