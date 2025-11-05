package com.dating.flairbit.service.interaction;

import java.util.UUID;

public interface InteractionService {
    void doMatchRequest(String from, String to, String intent);
}