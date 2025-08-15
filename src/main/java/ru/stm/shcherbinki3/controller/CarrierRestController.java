package ru.stm.shcherbinki3.controller;

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

    @PostMapping("/carrier/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createCarrier(@RequestParam Long userId, @Valid @RequestBody CarrierDto carrierDto) {
        return ResponseEntity.created(URI.create("/api/v1/carrier/" + carrierService.create(carrierDto, userId))).build();
    }

    @DeleteMapping("/carrier")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> softDelete(@RequestParam Long userId) {
        carrierService.softDelete(userId);
        return ResponseEntity.ok("The carrier was successfully deleted!");
    }

    @PatchMapping("/carrier/restore")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> softRecover(@RequestParam Long userId) {
        carrierService.softRestore(userId);
        return ResponseEntity.ok("The carrier was successfully deleted!");
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
