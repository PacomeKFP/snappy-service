package inc.yowyob.service.snappy.domain.usecases;

import reactor.core.publisher.Mono;

public interface UseCase<D, P> {
  Mono<P> execute(D dto);
}
