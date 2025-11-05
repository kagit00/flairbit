package com.dating.flairbit.controller.interaction;

import com.dating.flairbit.async.FlairBitProducer;
import com.dating.flairbit.service.interaction.InteractionService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/users/{from}/")
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;


}
