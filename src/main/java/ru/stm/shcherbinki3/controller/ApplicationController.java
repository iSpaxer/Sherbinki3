package ru.stm.shcherbinki3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.stm.shcherbinki3.util.ApplicationDataComponent;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {

    // todo Авторизация Для админов можно было бы показывать более крутое описание версий
    private final ApplicationDataComponent applicationDataComponent;

    @GetMapping("/info")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> versionAPI() {
        return ResponseEntity.ok(applicationDataComponent);
    }


}
