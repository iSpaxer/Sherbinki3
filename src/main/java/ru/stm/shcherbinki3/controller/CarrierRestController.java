package ru.stm.shcherbinki3.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dto.CarrierDto;
import ru.stm.shcherbinki3.service.CarrierService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class CarrierRestController {

    private final CarrierService carrierService;

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




//////
//    @PostMapping("/carrier/{carrier}/route/create")
//    @ResponseStatus(HttpStatus.CREATED)
//    // todo AOP ну не фильтры, валидация через интерсепторы
//    public ResponseEntity<CarrierDto> createCarrier(Auth auth, CarrierDto carrierDto) {
//        carrierService.create(carrierDto, auth);
//    }
//
//    @GetMapping("/carrier/routes")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ResponseEntity<CarrierDto> getRoutes(Auth auth, CarrierDto carrierDto) {
//        carrierService.create(carrierDto, auth);
//    }

}
