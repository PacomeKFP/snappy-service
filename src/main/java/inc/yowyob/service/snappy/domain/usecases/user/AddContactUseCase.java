package inc.yowyob.service.snappy.domain.usecases.user;

import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.FluxUseCase;
import inc.yowyob.service.snappy.infrastructure.repositories.UserRepository;
import inc.yowyob.service.snappy.infrastructure.repositories.UserContactRepository;
import inc.yowyob.service.snappy.presentation.dto.user.AddContactDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AddContactUseCase implements FluxUseCase<AddContactDto, User> {

  private final UserRepository userRepository;
  private final UserContactRepository userContactRepository;

  public AddContactUseCase(UserRepository userRepository, UserContactRepository userContactRepository) {
    this.userRepository = userRepository;
    this.userContactRepository = userContactRepository;
  }

  @Override
  public Flux<User> execute(AddContactDto dto) {
    // Validate the requesting user and contact exist
    Mono<User> requesterMono = userRepository
        .findByExternalIdAndProjectId(dto.getRequesterId(), dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Requester not found in the project")));
        
    Mono<User> contactMono = userRepository
        .findByExternalIdAndProjectId(dto.getContactId(), dto.getProjectId())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Contact not found in the project")));

    return Mono.zip(requesterMono, contactMono)
        .flatMap(tuple -> {
            User requester = tuple.getT1();
            User contact = tuple.getT2();
            
            // Add the contact relationship in the user_contacts table
            return userContactRepository.addContact(requester.getId(), contact.getId())
                .then(Mono.just(contact));
        })
        .flux();
  }
}