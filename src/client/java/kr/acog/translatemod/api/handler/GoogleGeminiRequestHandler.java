package kr.acog.translatemod.api.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.acog.translatemod.api.TranslateHandler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GoogleGeminiRequestHandler {

    private static final String[] SAFETY_CATEGORIES = {
            "HARM_CATEGORY_HARASSMENT",
            "HARM_CATEGORY_HATE_SPEECH",
            "HARM_CATEGORY_SEXUALLY_EXPLICIT",
            "HARM_CATEGORY_DANGEROUS_CONTENT"
    };

    public static CompletableFuture<String> request(TranslateHandler.TranslateData data) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + data.setting().model().getModelId() + ":generateContent?key=" + data.setting().currentApiKey();
        ObjectNode body = buildRequestBody(data);

        return ApiHttp.sendJsonRequest(
                url,
                Map.of(),
                body,
                GoogleGeminiRequestHandler::extractResponse,
                "Gemini"
        );
    }

    private static ObjectNode buildRequestBody(TranslateHandler.TranslateData data) {
        ObjectNode root = ApiHttp.MAPPER.createObjectNode();
        root.putArray("contents").addObject().putArray("parts").addObject().put("text", data.prompt());

        ArrayNode safetySettings = root.putArray("safetySettings");
        for (String category : SAFETY_CATEGORIES) {
            safetySettings.addObject().put("category", category).put("threshold", "BLOCK_NONE");
        }

        root.putObject("generationConfig").put("maxOutputTokens", data.setting().maxTokens());
        return root;
    }

    private static String extractResponse(JsonNode root) {
        JsonNode candidates = root.path("candidates");
        if (candidates.isMissingNode() || candidates.isEmpty()) {
            throw new RuntimeException("Content blocked by safety filter");
        }
        return candidates.get(0).path("content").path("parts").get(0).path("text").asText().trim();
    }

}
