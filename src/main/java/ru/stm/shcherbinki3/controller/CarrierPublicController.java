package ru.stm.shcherbinki3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dto.route.RouteWithCarrierDto;
import ru.stm.shcherbinki3.dto.ticket.TicketPublicDto;
import ru.stm.shcherbinki3.service.RouteService;
import ru.stm.shcherbinki3.service.TicketService;
import ru.stm.shcherbinki3.util.exception.ErrorResponse;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api" + "/v${app.version}/")
@Tag(name = "Public Carrier API", description = "API for retrieving public routes and tickets")
public class CarrierPublicController {

    private final RouteService routeService;
    private final TicketService ticketService;

    @Operation(
            summary = "Retrieve a paginated list of routes",
            description = """
                    Retrieves a paginated list of routes based on optional filters.
                    Filters include carrier name, departure location, destination location, and departure date.
                    The date must be today or in the future.
                    Pagination is controlled by page and size parameters.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Routes successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or date in the past",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/routes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<RouteWithCarrierDto>> getRoutes(
            @RequestParam(value = "carrier_name", required = false)
            @Parameter(description = "Carrier name filter (optional)", example = "TestCarrier")
            String carrierName,
            @RequestParam(required = false)
            @Parameter(description = "Departure location filter (optional)", example = "Moscow")
            String departure,
            @RequestParam(required = false)
            @Parameter(description = "Destination location filter (optional)", example = "Saint Petersburg")
            String destination,
            @RequestParam(required = false)
            @FutureOrPresent(message = "Date must be today or in the future")
            @Parameter(description = "Date of departure (format: YYYY-MM-DD), must be today or in the future (optional)",
                    schema = @Schema(type = "string", format = "date", example = "2025-08-20"))
            LocalDate date,
            @Parameter(description = "Pagination and sorting parameters (page, size, sort)", required = true)
            Pageable pageable
    ) {
        return ResponseEntity.ok(routeService.getRoutes(carrierName, departure, destination, date, pageable));
    }

    @Operation(
            summary = "Retrieve a paginated list of tickets for a route",
            description = """
                    Retrieves a paginated list of tickets for a specific route, with an optional date filter.
                    The date must be today or in the future.
                    Pagination is controlled by page and size parameters.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid route ID or date in the past",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Route not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/route/{id}/tickets")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<TicketPublicDto>> getTickets(
            @PathVariable("id")
            @Parameter(description = "ID of the route", required = true, example = "1")
            Long routeId,
            @RequestParam(required = false)
            @FutureOrPresent(message = "Date must be today or in the future")
            @Parameter(description = "Date of departure (format: YYYY-MM-DD), must be today or in the future (optional)",
                    schema = @Schema(type = "string", format = "date", example = "2025-08-20"))
            LocalDate date,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ticketService.getTickets(routeId, date, pageable));
    }
}