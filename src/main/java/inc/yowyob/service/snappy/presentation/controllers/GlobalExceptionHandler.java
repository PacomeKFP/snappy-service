package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.exceptions.AuthenticationFailedException;
import inc.yowyob.service.snappy.domain.exceptions.EntityNotFoundException;
import inc.yowyob.service.snappy.domain.exceptions.IllegalStateTransitionException;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    return Mono.just(ResponseEntity.badRequest().body(Map.of("error", ex.getMessage())));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleEntityNotFoundException(
      EntityNotFoundException ex) {
    return Mono.just(ResponseEntity.status(404).body(Map.of("error", ex.getMessage())));
  }

  @ExceptionHandler(AuthenticationFailedException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleAuthenticationFailedException(
      AuthenticationFailedException ex) {
    return Mono.just(ResponseEntity.status(403).body(Map.of("error", ex.getMessage())));
  }

  @ExceptionHandler(IllegalStateTransitionException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleIllegalStateTransitionException(
      IllegalStateTransitionException ex) { // Corrected exception type
    return Mono.just(ResponseEntity.status(409).body(Map.of("error", ex.getMessage())));
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, String>>> handleGeneralException(Exception ex) {
    // In a reactive context, logging should ideally be non-blocking.
    // For simplicity, ex.printStackTrace() is kept, but consider structured/async logging.
    ex.printStackTrace();
    return Mono.just(ResponseEntity.status(500).body(Map.of("error", ex.getMessage())));
  }
}
