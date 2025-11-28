package kr.acog.translatemod.api.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.acog.translatemod.api.TranslateHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GoogleGeminiRequestHandler {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static CompletableFuture<String> request(TranslateHandler.TranslateData data) {
        try {
            String apiKey = data.setting().key();
            String model = data.setting().model().getModelId();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            ObjectNode rootNode = mapper.createObjectNode();
            ArrayNode contentsNode = rootNode.putArray("contents");
            ObjectNode partsNode = contentsNode.addObject();
            ArrayNode partsArray = partsNode.putArray("parts");
            partsArray.addObject().put("text", data.prompt() + data.original());

            ObjectNode generationConfig = rootNode.putObject("generationConfig");
            generationConfig.put("maxOutputTokens", data.setting().maxTokens());

            String requestBody = mapper.writeValueAsString(rootNode);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        try {
                            if (response.statusCode() != 200) {
                                throw new RuntimeException("API 오류: " + response.statusCode() + " " + response.body());
                            }
                            JsonNode responseNode = mapper.readTree(response.body());
                            return responseNode.path("candidates").get(0).path("content").path("parts").get(0)
                                    .path("text").asText().trim();
                        } catch (Exception e) {
                            System.out.println(e.getMessage()   );
                            throw new RuntimeException("응답 파싱 실패", e);
                        }
                    });

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}
