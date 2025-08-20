package ru.stm.shcherbinki3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.stm.shcherbinki3.service.TicketService;
import ru.stm.shcherbinki3.util.exception.ErrorResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api" + "/v${app.version}/ticket")
@Tag(name = "Ticket Management API", description = "API for purchasing and returning tickets")
public class TicketRestController {

    private final TicketService ticketService;

    @Operation(
            summary = "Purchase a ticket",
            description = """
                    Purchases a ticket for a specified user. The ticket must not already be purchased.
                    The user ID and ticket ID must be positive numbers.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket successfully purchased",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Ticket 1 has been purchased"))),
            @ApiResponse(responseCode = "400", description = "Invalid user ID, ticket ID, or ticket already purchased",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/buy")
    public ResponseEntity<String> buyTicket(
            @RequestParam
            @Positive(message = "User ID must be a positive number")
            @Parameter(description = "ID of the user purchasing the ticket", required = true, example = "1")
            Long userId,
            @RequestParam
            @Positive(message = "Ticket ID must be a positive number")
            @Parameter(description = "ID of the ticket to purchase", required = true, example = "1")
            Long ticketId) {
        ticketService.buyTicket(userId, ticketId);
        return ResponseEntity.ok("Ticket %d has been purchased".formatted(ticketId));
    }

    @Operation(
            summary = "Return a purchased ticket",
            description = """
                    Returns a previously purchased ticket, unassigning it from the user.
                    The ticket must be owned by the specified user.
                    The user ID and ticket ID must be positive numbers.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket successfully returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Ticket 1 has been returned"))),
            @ApiResponse(responseCode = "400", description = "Invalid user ID, ticket ID, ticket not purchased, or not owned by the user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/return")
    public ResponseEntity<String> returnTicket(
            @RequestParam
            @Positive(message = "User ID must be a positive number")
            @Parameter(description = "ID of the user returning the ticket", required = true, example = "1")
            Long userId,
            @RequestParam
            @Positive(message = "Ticket ID must be a positive number")
            @Parameter(description = "ID of the ticket to return", required = true, example = "1")
            Long ticketId) {
        ticketService.returnTicket(userId, ticketId);
        return ResponseEntity.ok("Ticket %d has been returned".formatted(ticketId));
    }
}