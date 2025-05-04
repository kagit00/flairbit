package com.dating.flairbit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Custom annotation to validate that a string is a valid name.
 * A valid name can contain alphabetic characters (including accented characters),
 * apostrophes, hyphens, and spaces.
 *
 * This annotation is used in conjunction with the {@link NameValidator} class to
 * perform the validation logic.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NameValidator.class)
public @interface ValidName {

    /**
     * Default error message to be used when the validation fails.
     *
     * @return the default error message
     */
    String message() default "Invalid Name Format.";

    /**
     * Groups allow you to categorize the validation constraints.
     *
     * @return an array of groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload allows you to carry additional data with the constraint.
     *
     * @return an array of Payload
     */
    Class<? extends Payload>[] payload() default {};
}

