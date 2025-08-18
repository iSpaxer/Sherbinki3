package ru.stm.shcherbinki3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticket")
public class TicketRestController {

//    private final TicketService ticketService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyTicket(Long userId,
                                       @RequestParam Long tickedId) {
        return null;
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnTicket(Long userId,
                                          @RequestParam Long tickedId) {
        return null;
    }

}
