package ru.stm.shcherbinki3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dto.UserDto;
import ru.stm.shcherbinki3.dto.carrier.CarrierWithRoutesDto;
import ru.stm.shcherbinki3.dto.ticket.TicketPublicDto;
import ru.stm.shcherbinki3.dto.ticket.TicketPurchasedDto;
import ru.stm.shcherbinki3.service.CarrierService;
import ru.stm.shcherbinki3.service.TicketService;
import ru.stm.shcherbinki3.service.UserService;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "API for managing users")
public class UserRestController {

    private final UserService userService;
    private final TicketService ticketService;
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


    @GetMapping("/tickets")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<TicketPurchasedDto>> getTickets(
            Long userId,
            @RequestParam(required = false)
            @Parameter(description = "Date of after (format: YYYY-MM-DD)",
                    schema = @Schema(type = "string", format = "date", example = "2025-07-19"))
            LocalDate after,
            @RequestParam(required = false)
            @Parameter(description = "Date of before (format: YYYY-MM-DD)",
                    schema = @Schema(type = "string", format = "date", example = "2025-08-19"))LocalDate before,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                ticketService.getTicketsByUser(userId, after, before, pageable));

    }



}
