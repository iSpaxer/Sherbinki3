package ru.stm.shcherbinki3.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dto.CarrierDto;
import ru.stm.shcherbinki3.model.Carrier;
import ru.stm.shcherbinki3.service.CarrierService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class CarrierRestController {

    private final CarrierService carrierService;

    @PostMapping("/carrier/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CarrierDto> createCarrier(Auth auth, CarrierDto carrierDto) {
        carrierService.create(carrierDto, auth);
    }

    @PostMapping("/carrier/{carrier}/route/create")
    @ResponseStatus(HttpStatus.CREATED)
    // todo AOP ну не фильтры, валидация через интерсепторы
    public ResponseEntity<CarrierDto> createCarrier(Auth auth, CarrierDto carrierDto) {
        carrierService.create(carrierDto, auth);
    }

    @GetMapping("/carrier/routes")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CarrierDto> getRoutes(Auth auth, CarrierDto carrierDto) {
        carrierService.create(carrierDto, auth);
    }

}
