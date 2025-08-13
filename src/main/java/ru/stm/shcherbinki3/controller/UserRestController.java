package ru.stm.shcherbinki3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.models.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dto.TicketDto;
import ru.stm.shcherbinki3.dto.UserDto;
import ru.stm.shcherbinki3.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserRestController {

    private final UserService userService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createUser(@Valid UserDto dto) {
        userService.create(dto);
        return ResponseEntity.ok()
                .build();
    }

    @GetMapping({"/{id:[1-9]\\d*}", ""})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserDto> getUser(@PathVariable(required = false) Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserDto> updateUser(UserDto dto) {
        userService.update(dto);
        return ResponseEntity.ok()
                .build();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> deleteUser() {
        return null;
    }

    // todo Pageable
    @GetMapping("/tickets")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TicketDto>> getTickets(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime travelDate,
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String carrierName,
            @RequestParam(required = false) boolean active,
            @RequestParam(required = false) boolean expired,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                userService.getTickets(travelDate, departure, destination, carrierName, active, expired, page, size));

    }



}
