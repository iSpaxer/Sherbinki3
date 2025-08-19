package ru.stm.shcherbinki3.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dto.carrier.CarrierDto;
import ru.stm.shcherbinki3.dto.route.RouteWithCarrierDto;
import ru.stm.shcherbinki3.dto.ticket.TicketCreateDto;
import ru.stm.shcherbinki3.service.CarrierService;
import ru.stm.shcherbinki3.service.RouteService;
import ru.stm.shcherbinki3.service.TicketService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class CarrierManagementRestController {

    private final CarrierService carrierService;
    private final RouteService routeService;
    private final TicketService ticketService;

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
    @PostMapping("/carrier/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createCarrier(@RequestParam Long userId, @Valid @RequestBody CarrierDto carrierDto) {
        return ResponseEntity.created(URI.create("/api/v1/carrier/" + carrierService.create(carrierDto, userId))).build();
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
    @DeleteMapping("/carrier")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> softDelete(@RequestParam Long userId) {
        carrierService.softDelete(userId);
        return ResponseEntity.ok("The carrier was successfully deleted!");
    }


    @Operation(
            summary = "Restore deleted carrier",
            description = """
        Restores a previously soft-deleted carrier within the 30-day grace period.
        If the grace period has expired, the carrier cannot be restored and the name may already be reserved by another user.
        """
    )
    @PatchMapping("/carrier/restore")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> softRecover(@RequestParam Long userId) {
        carrierService.softRestore(userId);
        return ResponseEntity.ok("The carrier was successfully restored!");
    }


    @PostMapping("/route/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createRoute(@RequestParam Long userId,
                                         @RequestBody RouteWithCarrierDto dto) {
        return ResponseEntity.created(URI.create("/api/v1/route/" + routeService.create(userId, dto))).build();
    }

    @PostMapping("/route/{id}/tickets/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createRouteTickets(@RequestParam Long userId,
                                         @PathVariable(value = "id") Long routeId,
                                         @RequestBody TicketCreateDto dto) {
        ticketService.create(userId, routeId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
