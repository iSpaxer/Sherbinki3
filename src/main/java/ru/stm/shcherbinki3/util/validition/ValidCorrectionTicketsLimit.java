package ru.stm.shcherbinki3.util.validition;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.stm.shcherbinki3.util.validition.validator.CorrectionTicketsLimitValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CorrectionTicketsLimitValidator.class)
public @interface ValidCorrectionTicketsLimit {
    String message() default "CorrectionTickets size cannot exceed quantityOfPlaces";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

