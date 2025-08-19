package ru.stm.shcherbinki3.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dao.TicketDao;
import ru.stm.shcherbinki3.dto.carrier.CarrierDto;
import ru.stm.shcherbinki3.dto.route.RouteFullDto;
import ru.stm.shcherbinki3.dto.route.RouteWithCarrierDto;
import ru.stm.shcherbinki3.dto.ticket.TicketPublicDto;
import ru.stm.shcherbinki3.service.RouteService;
import ru.stm.shcherbinki3.service.TicketService;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class CarrierPublicController {

    private final RouteService routeService;
    private final TicketService ticketService;

    @GetMapping("/routes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<RouteWithCarrierDto>> getRoutes(
            @RequestParam(value = "carrier_name", required = false) String carrierName,
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false)
                @FutureOrPresent(message = "Date must be today or in the future")
                @Parameter(description = "Date of departure (format: YYYY-MM-DD), must be today or in the future",
                    schema = @Schema(type = "string", format = "date", example = "2025-08-19"))
            LocalDate date,
            Pageable pageable
    ) {
        return ResponseEntity.ok(routeService.getRoutes(carrierName, departure, destination, date, pageable));
    }

    @GetMapping("/route/{id}/tickets")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<TicketPublicDto>> getTickets(
            @PathVariable("id") Long routeId,
            @RequestParam(required = false)
            @FutureOrPresent(message = "Date must be today or in the future")
            @Parameter(description = "Date of departure (format: YYYY-MM-DD), must be today or in the future",
                    schema = @Schema(type = "string", format = "date", example = "2025-08-19"))
            LocalDate date,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ticketService.getTickets(routeId, date, pageable));
    }

    @GetMapping("/carriers")
    public ResponseEntity<List<CarrierDto>> getAllCarriers() {
        return null;
    }

    // getTickets - возможно стоит переименовать API - для меня оно не очевидное
    @GetMapping("/route/{id}")
    public ResponseEntity<RouteFullDto> getRoute() {
        return null;
    }


}
