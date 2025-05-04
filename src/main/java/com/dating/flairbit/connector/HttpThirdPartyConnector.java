package com.dating.flairbit.connector;

import com.dating.flairbit.config.HttpApiProperties;
import com.dating.flairbit.dto.HttpRequest;
import com.dating.flairbit.dto.HttpResponse;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;
import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class HttpThirdPartyConnector implements ThirdPartyConnector<HttpRequest, HttpResponse> {

    private final OkHttpClient httpClient;
    private final HttpApiProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String integrationKey) {
        return properties.getConfigs().containsKey(integrationKey);
    }

    @Override
    public HttpResponse call(@Valid HttpRequest request) {
        HttpApiProperties.SingleApiConfig config = properties.getConfigs().get(request.getIntegrationKey());
        if (config == null) {
            throw new BadRequestException("Invalid integration key: " + request.getIntegrationKey());
        }

        log.info("Calling third-party API [{}]: {} {}", request.getIntegrationKey(), request.getMethod(), request.getPath());

        try {
            Request httpRequest = buildRequest(request, config);
            Response response = httpClient.newCall(httpRequest).execute();
            return handleResponse(response);

        } catch (IOException e) {
            log.error("API call failed for {}: {}", request.getIntegrationKey(), e.getMessage());
            throw new InternalServerErrorException("API call failed: " + e.getMessage());
        }
    }

    private Request buildRequest(HttpRequest request, HttpApiProperties.SingleApiConfig config) {
        String url = config.getBaseUrl() + request.getPath();
        Request.Builder builder = new Request.Builder().url(url);

        config.getHeaders().forEach(builder::addHeader);
        if (config.getApiKey() != null) {
            builder.addHeader("Authorization", "Bearer " + config.getApiKey());
        }

        String method = request.getMethod().toUpperCase();
        if ("POST".equals(method) || "PUT".equals(method)) {
            try {
                String jsonBody = objectMapper.writeValueAsString(request.getBody());
                RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
                builder.method(method, body);
            } catch (Exception e) {
                throw new BadRequestException("Invalid request body: " + e.getMessage());
            }
        } else if ("GET".equals(method)) {
            builder.get();
        } else {
            throw new BadRequestException("Unsupported HTTP method: " + method);
        }

        return builder.build();
    }

    private HttpResponse handleResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            log.error("API error: status={}, body={}",
                    response.code(), response.body() != null ? response.body().string() : "null");
            throw new InternalServerErrorException("API returned error: " + response.code());
        }

        try (ResponseBody body = response.body()) {
            if (body == null) {
                return HttpResponse.builder().statusCode(response.code()).build();
            }
            String responseBody = body.string();
            Object jsonBody = responseBody.isEmpty() ? null : objectMapper.readValue(responseBody, Object.class);
            return HttpResponse.builder()
                    .statusCode(response.code())
                    .body(jsonBody)
                    .build();
        }
    }
}

