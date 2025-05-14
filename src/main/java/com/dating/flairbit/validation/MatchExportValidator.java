package com.dating.flairbit.validation;

import com.dating.flairbit.dto.MatchSuggestionsExchange;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MatchExportValidator {

    public static boolean isValidPayload(MatchSuggestionsExchange message) {
        return message != null
                && message.getFilePath() != null
                && !message.getFilePath().isBlank()
                && message.getFileName() != null
                && !message.getFileName().isBlank();
    }
}
