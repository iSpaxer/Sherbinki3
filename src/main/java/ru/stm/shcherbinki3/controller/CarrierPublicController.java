package ru.stm.shcherbinki3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stm.shcherbinki3.dto.CarrierDto;
import ru.stm.shcherbinki3.dto.route.RouteDto;
import ru.stm.shcherbinki3.dto.route.RouteFullDto;
import ru.stm.shcherbinki3.service.RouteService;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class CarrierPublicController {

    private final RouteService routeService;

    @GetMapping("/routes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<RouteDto>> getRoutes(
            @RequestParam(value = "carrier_name", required = false) String carrierName,
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            Pageable pageable
    ) {
        return ResponseEntity.ok(routeService.getRoutes(carrierName, departure, destination, pageable));
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
