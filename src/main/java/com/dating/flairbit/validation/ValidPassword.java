package com.dating.flairbit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to validate that a password meets specific security criteria.
 * A valid password must contain at least 9 characters, including:
 * - At least one special character
 * - At least one capital letter
 * - At least one number
 *
 * This annotation works in conjunction with the {@link PasswordValidator} class to
 * implement the validation logic.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {

    /**
     * Default error message to be used when the password does not meet the required criteria.
     *
     * @return the default error message
     */
    String message() default "Invalid Password. Password must contain at least 9 characters including at least one special character, one capital letter, and one number.";

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
