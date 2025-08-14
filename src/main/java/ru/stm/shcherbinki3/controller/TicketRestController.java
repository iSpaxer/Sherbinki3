package ru.stm.shcherbinki3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.stm.shcherbinki3.dto.TicketDto;
import ru.stm.shcherbinki3.service.TicketService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticket")
public class TicketRestController {

//    private final TicketService ticketService;
//
//    @PostMapping("/buy")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<TicketDto> buyTicket(TicketDto dto) { // todo мне кажется я не ticketDto должен принимать
//        return ResponseEntity.ok(ticketService.butTicket(dto, authUser));
//    }
//
//    @PostMapping("/refund")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<TicketDto> refundTicket(TicketDto dto) { // todo вот тут 100% принимает билет
//
//    }

}
