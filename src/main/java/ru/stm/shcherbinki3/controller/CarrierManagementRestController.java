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
import ru.stm.shcherbinki3.dto.carrier.CarrierCreateDto;
import ru.stm.shcherbinki3.dto.route.RouteWithCarrierDto;
import ru.stm.shcherbinki3.dto.ticket.TicketCreateDto;
import ru.stm.shcherbinki3.security.DefaultAuthenticationPrincipal;
import ru.stm.shcherbinki3.service.CarrierService;
import ru.stm.shcherbinki3.service.RouteService;
import ru.stm.shcherbinki3.service.TicketService;
import ru.stm.shcherbinki3.util.exception.ErrorResponse;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@RequestMapping("/api" + "/v${app.version}/")
@Tag(name = "Carrier Management API", description = "API for managing carriers, routes, and tickets")
public class CarrierManagementRestController {

    private final CarrierService carrierService;
    private final RouteService routeService;
    private final TicketService ticketService;

    @Value("${app.version}")
    private String version;

    @Operation(
            summary = "Create a new carrier",
            description = """
                    Creates a new carrier with a unique name.
                    Each carrier can have only one owner.
                    If a carrier was previously deleted by the user, the old one is permanently removed when a new one is created.
                    Deleted carrier names remain reserved for 30 days, during which the original owner can restore them.
                    After 30 days, the name becomes available for other users.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Carrier successfully created",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid carrier data, or malformed request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Carrier name already taken",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/carrier/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createCarrier(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal,
            @Valid @RequestBody
            @Parameter(description = "Carrier data", required = true)
            CarrierCreateDto carrierDto
    ) {
        return ResponseEntity.created(URI.create("/api/" + version + "/carrier/" + carrierService.create(carrierDto, defaultAuthenticationPrincipal.getId())))
                .build();
    }

    @Operation(
            summary = "Update an existing carrier",
            description = """
                    Updates the details of an existing carrier associated with the specified user.
                    Only the carrier owner can update the carrier.
                    The carrier name must remain unique.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrier successfully updated",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Carrier has been updated!"))),
            @ApiResponse(responseCode = "400", description = "Invalid carrier data or user is not the owner",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or carrier not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Carrier name already taken",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/carrier")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateCarrier(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal,
            @Valid @RequestBody
            @Parameter(description = "Updated carrier data", required = true)
            CarrierCreateDto carrierDto
    ) {
        carrierService.update(carrierDto, defaultAuthenticationPrincipal.getId());
        return ResponseEntity.ok("Carrier has been updated!");
    }

    @Operation(
            summary = "Soft delete carrier",
            description = """
                    Soft deletes the carrier associated with the given user.
                    After deletion, the carrier name is reserved for 30 days.
                    During this period, the original owner can restore the carrier.
                    After 30 days, the name becomes available for other users.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrier successfully deleted",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "The carrier was successfully deleted!"))),
            @ApiResponse(responseCode = "400", description = "Invalid user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or carrier not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/carrier")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> softDelete(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal
            ) {
        carrierService.softDelete(defaultAuthenticationPrincipal.getId());
        return ResponseEntity.ok("The carrier was successfully deleted!");
    }

    @Operation(
            summary = "Restore deleted carrier",
            description = """
                    Restores a previously soft-deleted carrier within the 30-day grace period.
                    If the grace period has expired, the carrier cannot be restored and the name may already be reserved by another user.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrier successfully restored",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "The carrier was successfully restored!"))),
            @ApiResponse(responseCode = "400", description = "Invalid user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or carrier not found, or carrier not deleted",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/carrier/restore")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> softRecover(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal
            ) {
        carrierService.softRestore(defaultAuthenticationPrincipal.getId());
        return ResponseEntity.ok("The carrier was successfully restored!");
    }

    @Operation(
            summary = "Create a new route",
            description = """
                    Creates a new route for a carrier associated with the specified user.
                    Each carrier can have multiple routes, and each route can have tickets for different days.
                    The user must be the owner of the carrier.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Route successfully created",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid route data, or user is not the owner",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or carrier not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/route/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createRoute(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal,
            @RequestBody
            @Parameter(description = "Route data", required = true)
            RouteWithCarrierDto dto
    ) {
        return ResponseEntity.created(URI.create("/api/v1/route/" + routeService.create(defaultAuthenticationPrincipal.getId(), dto)))
                .build();
    }

    @Operation(
            summary = "Delete a route",
            description = """
                    Deletes a route and all associated tickets.
                    The user must be the owner of the carrier associated with the route.
                    Deleted routes cannot be restored.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route and associated tickets successfully deleted",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "The route has been deleted along with the tickets"))),
            @ApiResponse(responseCode = "400", description = "Route ID, or user is not the owner",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/route/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteRoute(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal,
            @PathVariable(value = "id")
            @Parameter(description = "ID of the route to delete", required = true, example = "1")
            Long routeId
    ) {
        routeService.deleteByRouteId(defaultAuthenticationPrincipal.getId(), routeId);
        return ResponseEntity.ok("The route has been deleted along with the tickets");
    }

    @Operation(
            summary = "Update a route's duration",
            description = """
                    Updates the duration (in minutes) of a route.
                    Only the durationMinutes field of the route can be updated.
                    The user must be the owner of the carrier associated with the route.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route duration successfully updated",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Travel time has been updated"))),
            @ApiResponse(responseCode = "400", description = "Invalid route ID, duration, or user is not the owner",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/route/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateRoute(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal,
            @PathVariable(value = "id")
            @Parameter(description = "ID of the route to update", required = true, example = "1")
            Long routeId,
            @RequestBody
            @Parameter(description = "New duration in minutes", required = true, example = "120")
            Long durationMinutes
    ) {
        routeService.updateRoute(defaultAuthenticationPrincipal.getId(), routeId, durationMinutes);
        return ResponseEntity.ok("Travel time has been updated");
    }

    @Operation(
            summary = "Create tickets for a route",
            description = """
                    Creates tickets for a specific route on different days.
                    The user must be the owner of the carrier associated with the route.
                    Tickets are created based on the provided ticket data.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tickets successfully created",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid route ID, ticket data, or user is not the owner",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or route not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/route/{id}/tickets/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createRouteTickets(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal defaultAuthenticationPrincipal,
            @PathVariable(value = "id")
            @Parameter(description = "ID of the route for which tickets are created", required = true, example = "1")
            Long routeId,
            @Valid @RequestBody
            @Parameter(description = "Ticket data", required = true)
            TicketCreateDto dto
    ) {
        ticketService.create(defaultAuthenticationPrincipal.getId(), routeId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }
}