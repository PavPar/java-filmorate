package ru.yandex.practicum.filmorate.anotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateAfterValidatior.class)
public @interface DateAfter {
    String message() default "{message.id}";

    String datestring() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
