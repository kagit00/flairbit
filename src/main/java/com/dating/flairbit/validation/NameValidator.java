package com.dating.flairbit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator class that checks if a given string is a valid name.
 * A valid name contains only alphabetic characters (including accented characters),
 * apostrophes, hyphens, and spaces.
 */
public class NameValidator implements ConstraintValidator<ValidName, String> {

    /**
     * Validates if the provided string is a valid name.
     * A valid name is a non-empty string that contains only alphabetic characters,
     * accented characters, apostrophes, hyphens, and spaces.
     *
     * @param value the string value to validate
     * @param context the context in which the constraint is evaluated
     * @return true if the value is a valid name, false otherwise
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !StringUtils.isEmpty(value) && value.matches("^[A-Za-zÀ-ÖØ-öø-ÿ'’\\- ]+$");
    }
}

