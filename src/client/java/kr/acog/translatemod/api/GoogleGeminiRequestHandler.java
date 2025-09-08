package kr.acog.translatemod.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GoogleGeminiRequestHandler {

    //AIzaSyDqsJTtGMGnQc_EPUZfn97XZF1WxOe9vH4

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String url = "https://generativelanguage.googleapis.com/v1beta/models/ㅇ:generateContent?key=";

    public static CompletableFuture<String> request(TranslateHandler.TranslateData data) {
        try {
            TranslateRequest requestBody = new TranslateRequest(
                    data.prompt() + data.original()
            );

            String json = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + data.setting().key()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(respone -> {
                        if (respone.statusCode() != 200) {
                            System.out.println(respone.body());
                            return "Failed Translate: HTTP " + respone.statusCode();
                        }
                        return extractFromResponseBody(respone.body());
                    });
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Failed Translate");
        }
    }

    private static String extractFromResponseBody(String body) {
        try {
            return objectMapper.readTree(body)
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText()
                    .trim();
        } catch (JsonProcessingException e) {
            return "Failed Translate";
        }
    }

    private record TranslateRequest(List<TranslateContent> contents) {

        public TranslateRequest(String content) {
            this(List.of(new TranslateContent(content)));
        }

    }

    private record TranslateContent(List<TranslatePart> parts) {

        public TranslateContent(String text) {
            this(List.of(new TranslatePart(text)));
        }

    }

    private record TranslatePart(String text) {}

}
