package ru.stm.shcherbinki3.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.stm.shcherbinki3.dto.ticket.TicketCreateDto;
import ru.stm.shcherbinki3.dto.ticket.TicketPublicDto;
import ru.stm.shcherbinki3.model.Ticket;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    default List<Ticket> toEntityList(TicketCreateDto ticketCreateDto) {
        Map<Integer, BigDecimal> correctionMap = Optional.ofNullable(ticketCreateDto.getCorrectionTickets())
                .orElse(Set.of())
                .stream()
                .collect(Collectors.toMap(
                        TicketCreateDto.CorrectionTickets::getPlace,
                        TicketCreateDto.CorrectionTickets::getPrice
                ));

        return IntStream.rangeClosed(1, ticketCreateDto.getQuantityOfPlaces())
                .mapToObj(placeNumber ->
                                  Ticket.builder()
                                          .placeNumber(placeNumber)
                                          .price(correctionMap.getOrDefault(placeNumber,
                                                                            ticketCreateDto.getCommonPrice()))
                                          .departureDatetime(ticketCreateDto.getDepartureDatetime())
                                          .build()
                )
                .toList();
    }

    @Mapping(target = "isPurchased", expression = "java(ticket.getUser() != null)")
    TicketPublicDto toDto(Ticket ticket);

    List<TicketPublicDto> toDtoList(List<Ticket> ticketList);

}
