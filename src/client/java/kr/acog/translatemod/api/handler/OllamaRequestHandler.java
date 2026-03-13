package kr.acog.translatemod.api.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.acog.translatemod.api.TranslateHandler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OllamaRequestHandler {

    public static CompletableFuture<String> request(TranslateHandler.TranslateData data) {
        ObjectNode body = ApiHttp.MAPPER.createObjectNode();
        body.put("model", data.setting().customModelId());
        body.put("stream", false);
        body.putArray("messages").addObject()
                .put("role", "user")
                .put("content", data.prompt());

        return ApiHttp.sendJsonRequest(
                data.setting().baseUrl() + "/api/chat",
                Map.of(),
                body,
                root -> root.path("message").path("content").asText().trim(),
                "Ollama"
        );
    }

}
