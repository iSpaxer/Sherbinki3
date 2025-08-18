package ru.stm.shcherbinki3.util.validition.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.stm.shcherbinki3.dto.ticket.TicketCreateDto;
import ru.stm.shcherbinki3.util.validition.ValidCorrectionTicketsLimit;

public class CorrectionTicketsLimitValidator implements ConstraintValidator<ValidCorrectionTicketsLimit, TicketCreateDto> {

    @Override
    public boolean isValid(TicketCreateDto dto, ConstraintValidatorContext context) {
        if (dto == null) return true;
        if (dto.getCorrectionTickets() == null) return true;
        if (dto.getQuantityOfPlaces() == null) return true;

        return dto.getCorrectionTickets().size() <= dto.getQuantityOfPlaces();
    }
}
