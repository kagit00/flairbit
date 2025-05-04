package com.dating.flairbit.controller;

import com.dating.flairbit.dto.Heartbeat;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/")
@RestController
@Tag(name = "HeartBeat API", description = "Operations related to HeartBeat of the server")
public class HeartbeatController {


    @GetMapping(value = "heartbeat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Heartbeat> checkHeartbeat() {
        Heartbeat hb = new Heartbeat("UP", "Server is running");
        return new ResponseEntity<>(hb, HttpStatusCode.valueOf(200));
    }
}
