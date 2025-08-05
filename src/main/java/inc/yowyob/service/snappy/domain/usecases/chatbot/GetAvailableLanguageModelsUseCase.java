package inc.yowyob.service.snappy.domain.usecases.chatbot;

import inc.yowyob.service.snappy.domain.entities.ChatbotLLM;
import inc.yowyob.service.snappy.domain.usecases.FluxUseCase;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class GetAvailableLanguageModelsUseCase implements FluxUseCase<Void, String> {

  @Override
  public Flux<String> execute(Void dto) {
    return Flux.fromIterable(Arrays.asList(ChatbotLLM.values()))
        .map(Enum::toString);
  }
}