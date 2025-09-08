package kr.acog.translatemod.api;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenRouterRequestHandler {

    //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

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String url = "https://openrouter.ai/api/v1/chat/completions";

    public static CompletableFuture<String> request(TranslateHandler.TranslateData data) {
        try {
            TranslateRequest requestBody = new TranslateRequest(
                    "google/gemma-3n-e4b-it:free",
                    data.prompt() + data.original()
            );

            String json = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + data.setting().key())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            return "Failed Translate: HTTP " + response.statusCode();
                        }
                        return extractFromResponseBody(response.body());
                    });
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Failed Translate");
        }
    }

    private static String extractFromResponseBody(String body) {
        try {
            return objectMapper.readTree(body)
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();
        } catch (JsonProcessingException e) {
            return "Failed Translate";
        }
    }

    private record TranslateRequest(
            String model,
            List<TranslateMessage> messages
    ) {

        public TranslateRequest(String model, String content) {
            this(model, List.of(new TranslateMessage(TranslateMessage.Role.USER, content)));
        }

    }

    private record TranslateMessage(
            Role role,
            String content
    ) {

        public enum Role {
            USER,
            SYSTEM,
            ASSISTANT;

            @JsonValue
            public String toLowerCase() {
                return toString().toLowerCase();
            }
        }

    }

}
