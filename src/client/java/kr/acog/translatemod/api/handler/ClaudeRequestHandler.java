package kr.acog.translatemod.api.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.acog.translatemod.api.TranslateHandler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ClaudeRequestHandler {

    public static CompletableFuture<String> request(TranslateHandler.TranslateData data) {
        ObjectNode body = ApiHttp.MAPPER.createObjectNode();
        body.put("model", data.setting().model().getModelId());
        body.put("max_tokens", data.setting().maxTokens());
        body.putArray("messages").addObject()
                .put("role", "user")
                .put("content", data.prompt());

        return ApiHttp.sendJsonRequest(
                "https://api.anthropic.com/v1/messages",
                Map.of("x-api-key", data.setting().currentApiKey(), "anthropic-version", "2023-06-01"),
                body,
                root -> root.path("content").get(0).path("text").asText().trim(),
                "Claude"
        );
    }

}
