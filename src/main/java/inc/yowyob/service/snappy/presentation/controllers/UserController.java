package inc.yowyob.service.snappy.presentation.controllers;

import inc.yowyob.service.snappy.domain.entities.User;
import inc.yowyob.service.snappy.domain.usecases.user.*;
import inc.yowyob.service.snappy.presentation.dto.user.AddContactDto;
import inc.yowyob.service.snappy.presentation.dto.user.CreateUserDto;
import inc.yowyob.service.snappy.presentation.dto.user.*;
import jakarta.validation.Valid;
// import java.util.List; // Replaced by Flux
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/users")
public class UserController {

  private final AddContactUseCase addContactUseCase;
  private final CreateUserUseCase createUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final FindAllUsersUseCase findAllUsersUseCase;
  private final GetUserContactsUseCase getUserContactsUseCase;
  private final FindUserByDisplayNameUseCase findUserByDisplayNameUseCase;

  public UserController(
      AddContactUseCase addContactUseCase,
      CreateUserUseCase createUserUseCase,
      DeleteUserUseCase deleteUserUseCase,
      FindAllUsersUseCase findAllUsersUseCase,
      GetUserContactsUseCase getUserContactsUseCase,
      FindUserByDisplayNameUseCase findUserByDisplayNameUseCase) {
    this.addContactUseCase = addContactUseCase;
    this.createUserUseCase = createUserUseCase;
    this.deleteUserUseCase = deleteUserUseCase;
    this.findAllUsersUseCase = findAllUsersUseCase;
    this.getUserContactsUseCase = getUserContactsUseCase;
    this.findUserByDisplayNameUseCase = findUserByDisplayNameUseCase;
  }

  /** Add a contact to the user's contact list. */
  @PostMapping("/add-contact")
  public Mono<ResponseEntity<Flux<User>>> addContact(
      @Valid @RequestBody Mono<AddContactDto> dtoMono) {
    // Assuming addContactUseCase.execute will return Flux<User>
    return dtoMono.flatMap(addContactUseCase::execute).map(ResponseEntity::ok);
  }

  /** Create a new user in the system. */
  @PostMapping("/create")
  public Mono<ResponseEntity<User>> createUser(@Valid @RequestBody Mono<CreateUserDto> dtoMono) {
    // Assuming createUserUseCase.execute will return Mono<User>
    return dtoMono.flatMap(createUserUseCase::execute).map(ResponseEntity::ok);
  }

  /** Retrieve all users for a project ID. */
  @GetMapping("/find-all")
  public Mono<ResponseEntity<Flux<User>>> findAllUsers(@RequestParam String projectId) {
    // Assuming findAllUsersUseCase.execute will return Flux<User>
    return Mono.fromCallable(() -> findAllUsersUseCase.execute(projectId)).map(ResponseEntity::ok);
  }

  @PostMapping("/filter/display-name")
  public Mono<ResponseEntity<Flux<User>>> filterUser(
      @Valid @RequestBody Mono<FindUserByDisplayNameDto> dtoMono) {
    // Assuming findUserByDisplayNameUseCase.execute will return Flux<User>
    return dtoMono.flatMap(findUserByDisplayNameUseCase::execute).map(ResponseEntity::ok);
  }

  /**
   * Get the contact list of a user based on their external ID and project ID.
   *
   * @param dtoMono Contains userExternalId and projectId.
   * @return List of contacts associated with the user.
   */
  @PostMapping("/get-contacts")
  public Mono<ResponseEntity<Flux<User>>> getUserContacts(
      @Valid @RequestBody Mono<GetUserContactsDto> dtoMono) {
    // Assuming getUserContactsUseCase.execute will return Flux<User>
    return dtoMono.flatMap(getUserContactsUseCase::execute).map(ResponseEntity::ok);
  }

  /** Delete a user by their unique ID. */
  @DeleteMapping("/delete/{userId}")
  public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String userId) {
    // Assuming deleteUserUseCase.execute will return Mono<Void>
    return deleteUserUseCase.execute(userId).then(Mono.just(ResponseEntity.noContent().build()));
  }
}
