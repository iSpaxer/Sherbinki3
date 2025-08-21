package ru.stm.shcherbinki3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dto.LoginRequest;
import ru.stm.shcherbinki3.dto.UserDto;
import ru.stm.shcherbinki3.dto.carrier.CarrierWithRoutesDto;
import ru.stm.shcherbinki3.dto.ticket.TicketPurchasedDto;
import ru.stm.shcherbinki3.security.DefaultAuthenticationPrincipal;
import ru.stm.shcherbinki3.service.CarrierManagementService;
import ru.stm.shcherbinki3.service.CarrierService;
import ru.stm.shcherbinki3.service.TicketService;
import ru.stm.shcherbinki3.service.UserService;
import ru.stm.shcherbinki3.util.exception.ErrorResponse;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api" + "/v${app.version}/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "API for managing users")
public class UserRestController {

    private final UserService userService;
    private final TicketService ticketService;
    private final CarrierService carrierService;
    private final CarrierManagementService carrierManagementService;

    @Value("${app.version}")
    private String version;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a user",
            description = "Creates a new user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully created",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid user data or malformed request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email is already taken or used by a deleted user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto dto) {
        return ResponseEntity
                .created(URI.create("/api/" + version + "/user/" + userService.create(dto)))
                .build();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get authenticated user ",
            description = "Retrieves user details by their JWT.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserDto> getUser(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal
    ) {
        return ResponseEntity.ok(userService.getById(defaultAuthenticationPrincipal.getId()));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update an existing user's information",
            description = "Updates the details of an existing user. All updatable fields must be provided.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email is already taken or used by a deleted user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserDto> updateUser(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal,
            @Valid @RequestBody
            @Parameter(description = "Updated user data", required = true)
            UserDto dto
    ) {
        return ResponseEntity.ok(userService.update(defaultAuthenticationPrincipal, dto));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Delete authenticated user",
            description = "Deletes the user by JWT. Returns a confirmation message.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully deleted",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Account would have been deleted"))),
            @ApiResponse(responseCode = "400", description = "Invalid user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or associated carrier not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> deleteUser(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal
    ) {
        carrierManagementService.deleteUserAndCarrier(defaultAuthenticationPrincipal.getId());
        return ResponseEntity.ok("Account would have been deleted");
    }

    @PatchMapping("/restore")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Restore a deleted user account",
            description = "Restores a previously deleted user account and, if applicable, their associated carrier. Returns a confirmation message upon success."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User account successfully restored",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Account would have been restored"))),
            @ApiResponse(responseCode = "400", description = "Invalid user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found or not deleted",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> restoreUser(
            @RequestBody LoginRequest dto
    ) {
        carrierManagementService.restoreUserAndCarrier(dto);
        return ResponseEntity.ok("Account would have been restored");
    }

    @GetMapping("/carrier")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get carrier with routes by user by JWT",
            description = "Retrieves the carrier associated by JWT, along with all its routes. Returns a CarrierWithRoutesDto containing carrier details and a list of routes.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrier and routes successfully retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarrierWithRoutesDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user ",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or carrier not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CarrierWithRoutesDto> getCarrier(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal
    ) {
        return ResponseEntity.ok(carrierService.getByUserId(defaultAuthenticationPrincipal.getId()));
    }

    @GetMapping("/tickets")
    @ResponseStatus(HttpStatus.OK)

    @Operation(
            summary = "Get purchased tickets by user by JWT with pagination",
            description = "Retrieves a paginated list of purchased tickets for the specified user with JWT. Supports optional date filters to narrow down results by ticket creation or travel date. Pagination is controlled via page and size parameters, with optional sorting.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of purchased tickets successfully retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user or pagination parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or tickets not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<TicketPurchasedDto>> getTickets(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal,
            @RequestParam(required = false)
            @Parameter(description = "Filter tickets created or traveled after this date (format: YYYY-MM-DD)", schema = @Schema(type = "string", format = "date", example = "2025-07-19"))
            LocalDate after,
            @RequestParam(required = false)
            @Parameter(description = "Filter tickets created or traveled before this date (format: YYYY-MM-DD)", schema = @Schema(type = "string", format = "date", example = "2025-08-19"))
            LocalDate before,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ticketService.getTicketsByUser(defaultAuthenticationPrincipal.getId(), after, before, pageable));
    }
}