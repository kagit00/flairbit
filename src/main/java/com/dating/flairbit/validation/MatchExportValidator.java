package com.dating.flairbit.validation;

import com.dating.flairbit.dto.NodeExchange;

public final class MatchExportValidator {

    public static boolean isValidPayload(NodeExchange message) {
        return message != null
                && message.getFileContent() != null
                && !message.getFileContent().isBlank()
                && message.getFileName() != null
                && !message.getFileName().isBlank();
    }
}
