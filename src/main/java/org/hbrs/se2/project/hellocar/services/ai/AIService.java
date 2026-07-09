package org.hbrs.se2.project.hellocar.services.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {
    //mit Doppelpunkt, das macht ihn optional!
    @Value("${kiconnect.api.key:}")
    private String apiKey;

    @Value("${kiconnect.api.url:}")
    private String apiUrl;

    @Value("${kiconnect.model:}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendMessageAndGetResponse(List<Map<String, String>> chatHistory) {
        pruefeKonfiguration();

        try {
            HttpHeaders header = buildHeader();
            String payload = buildPayload(chatHistory);
            HttpEntity<String> request = new HttpEntity<>(payload, header);

            String response = restTemplate.postForObject(apiUrl, request, String.class);

            if (response == null || response.isBlank()) {
                throw new RuntimeException("Leere Antwort vom KI-Dienst.");
            }

            return extractContent(response);

        } catch (Exception e) {
            throw new RuntimeException("Der Chatbot ist gerade nicht erreichbar.", e);
        }
    }

    private void pruefeKonfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("KICONNECT_API_KEY ist nicht gesetzt.");
        }

        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("kiconnect.api.url ist nicht gesetzt.");
        }

        if (model == null || model.isBlank()) {
            throw new IllegalStateException("kiconnect.model ist nicht gesetzt.");
        }
    }

    private HttpHeaders buildHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    private String buildPayload(List<Map<String, String>> chatHistory) {
        try {
            Map<String, Object> payload = Map.of(
                    "model", model,
                    "messages", chatHistory
            );

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(payload);

        } catch (Exception e) {
            throw new RuntimeException("Payload konnte nicht gebaut werden.", e);
        }
    }

    private String extractContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");

            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                throw new RuntimeException("Keine Chatbot-Antwort in der Response gefunden.");
            }

            return contentNode.asText();

        } catch (Exception e) {
            throw new RuntimeException("Response konnte nicht gelesen werden.", e);
        }
    }
}
