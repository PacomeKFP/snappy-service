package inc.yowyob.service.snappy.domain.usecases;

import reactor.core.publisher.Mono;

public interface MonoUseCase<D, P> {
  Mono<P> execute(D dto);
}