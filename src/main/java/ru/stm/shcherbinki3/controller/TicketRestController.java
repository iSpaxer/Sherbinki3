package ru.stm.shcherbinki3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.stm.shcherbinki3.service.TicketService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticket")
public class TicketRestController {

    private final TicketService ticketService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyTicket(Long userId,
                                       @RequestParam Long tickedId) {
        ticketService.buyTicket(userId, tickedId);
        return ResponseEntity.ok("Ticket %d has been purchased".formatted(tickedId));
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnTicket(Long userId,
                                          @RequestParam Long tickedId) {
        ticketService.returnTicket(userId, tickedId);
        return ResponseEntity.ok("Ticket %d has been returned".formatted(tickedId));
    }

}
