package com.dating.flairbit.connector;

import com.dating.flairbit.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class ThirdPartyConnectorDispatcher {

    private final List<ThirdPartyConnector<?, ?>> connectors;

    public <T, R> R dispatch(String integrationKey, T request) {
        log.debug("Dispatching request for integrationKey: {}", integrationKey);

        @SuppressWarnings("unchecked")
        ThirdPartyConnector<T, R> connector = connectors.stream()
                .filter(c -> c.supports(integrationKey))
                .map(c -> (ThirdPartyConnector<T, R>) c)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No connector found for key: {}", integrationKey);
                    return new BadRequestException("No connector found for key: " + integrationKey);
                });

        return connector.call(request);
    }
}
