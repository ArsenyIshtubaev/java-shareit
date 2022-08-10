package ru.practicum.shareit.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidatorImpl.class)
@Target( {ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailValidation {
    String message() default "Duplicate Email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
