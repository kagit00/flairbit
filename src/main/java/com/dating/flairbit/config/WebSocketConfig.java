package com.dating.flairbit.config;

import com.dating.flairbit.security.JwtStompAuthChannelInterceptor;
import com.dating.flairbit.security.JwtWebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.websocket.broker.type:simple}")
    private String brokerType;

    @Value("${stomp.relay.host:localhost}") private String relayHost;
    @Value("${stomp.relay.port:61613}") private int relayPort;
    @Value("${stomp.relay.client-login:guest}") private String clientLogin;
    @Value("${stomp.relay.client-passcode:guest}") private String clientPasscode;
    @Value("${stomp.relay.system-login:guest}") private String systemLogin;
    @Value("${stomp.relay.system-passcode:guest}") private String systemPasscode;

    private final JwtWebSocketHandshakeInterceptor handshakeInterceptor;
    private final JwtStompAuthChannelInterceptor jwtStompAuthChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws-direct")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(@NotNull MessageBrokerRegistry registry) {
        if ("relay".equalsIgnoreCase(brokerType)) {
            registry.enableStompBrokerRelay("/topic", "/queue")
                    .setRelayHost(relayHost)
                    .setRelayPort(relayPort)
                    .setClientLogin(clientLogin)
                    .setClientPasscode(clientPasscode)
                    .setSystemLogin(systemLogin)
                    .setSystemPasscode(systemPasscode)
                    .setSystemHeartbeatSendInterval(15000)
                    .setSystemHeartbeatReceiveInterval(15000);
        } else {
            registry.enableSimpleBroker("/topic", "/queue");
        }
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtStompAuthChannelInterceptor);
    }
}
