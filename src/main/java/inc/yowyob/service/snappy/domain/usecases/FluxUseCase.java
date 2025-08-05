package inc.yowyob.service.snappy.domain.usecases;

import reactor.core.publisher.Flux;

public interface FluxUseCase<D, P> {
  Flux<P> execute(D dto);
}