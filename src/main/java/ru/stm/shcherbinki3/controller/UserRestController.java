package ru.stm.shcherbinki3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dto.UserDto;
import ru.stm.shcherbinki3.dto.carrier.CarrierWithRoutesDto;
import ru.stm.shcherbinki3.service.CarrierService;
import ru.stm.shcherbinki3.service.UserService;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "API for managing users")
public class UserRestController {

    private final UserService userService;
    private final CarrierService carrierService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a restaurant for the user, additional owners can be added",
            description = "Creates a new user. Optionally, additional restaurant owners can be assigned to this user."
    )
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto dto) {
        return ResponseEntity
                .created(URI.create("/api/v1/user/" + userService.create(dto))).build();
    }

    @GetMapping({"/{id:[1-9]\\d*}", ""})
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get user by ID or the currently authenticated user if ID is omitted",
            description = "Retrieves user details by their ID. If no ID is provided, returns the currently authenticated user's information."
    )
    public ResponseEntity<UserDto> getUser(@PathVariable(required = false) Long id) {
        return ResponseEntity
                .ok(userService.getById(id));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update an existing user's information",
            description = "Updates the details of an existing user. All updatable fields must be provided."
    )
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto dto) {
        return ResponseEntity
                .ok(userService.update(dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Delete a user by ID",
            description = "Deletes the user with the specified ID. Returns a confirmation message."
    )
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity
                .ok("Account would have been deleted");
    }

    @GetMapping("/carrier")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CarrierWithRoutesDto> getCarrier(Long userId) {
        return ResponseEntity.ok(carrierService.getByUserId(userId));
    }

//
//    // todo Pageable
//    @GetMapping("/tickets")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<List<TicketDto>> getTickets(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime travelDate,
//            @RequestParam(required = false) String departure,
//            @RequestParam(required = false) String destination,
//            @RequestParam(required = false) String carrierName,
//            @RequestParam(required = false) boolean active,
//            @RequestParam(required = false) boolean expired,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        return ResponseEntity.ok(
//                userService.getTickets(travelDate, departure, destination, carrierName, active, expired, page, size));
//
//    }



}
