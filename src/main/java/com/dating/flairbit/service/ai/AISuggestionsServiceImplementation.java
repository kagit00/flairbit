package com.dating.flairbit.service.ai;

import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.models.AISuggestion;
import com.dating.flairbit.utils.Connector;
import com.dating.flairbit.utils.Constant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class AISuggestionsServiceImplementation implements AISuggestionsService {
    private final Environment environment;
    private final ObjectMapper objectMapper;


    @Override
    public AISuggestion generateSuggestions(String title, String sectionType) {
        String generatedText;
        HttpMethod methodType = HttpMethod.POST;
        String groqUrl = environment.getProperty("groq.api.url");
        String groqApiKey = environment.getProperty("groq.api.key");

        String response = Connector.performRequest(groqUrl, groqApiKey, title, sectionType, methodType);
        generatedText = parseResponse(response);
        log.debug(generatedText);

        return AISuggestion.builder().generatedSuggestion(generatedText).build();
    }

    private String parseResponse(String rawResponse) {
        String beautifiedResponse;
        if (Objects.isNull(rawResponse) || rawResponse.isEmpty())
            throw new InternalServerErrorException("No response received from the AI.");

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            if (root.has(Constant.CHOICES) && root.get(Constant.CHOICES).isArray()) {
                beautifiedResponse = root.get("choices").get(0).get("message").get("content").asText();
            } else {
                throw new InternalServerErrorException("Unexpected response format or empty array.");
            }
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
        log.debug(beautifiedResponse);
        return beautifiedResponse;
    }
}
