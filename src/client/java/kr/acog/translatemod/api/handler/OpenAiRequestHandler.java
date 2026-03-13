package kr.acog.translatemod.api.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.acog.translatemod.api.TranslateHandler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OpenAiRequestHandler {

    public static CompletableFuture<String> request(TranslateHandler.TranslateData data) {
        ObjectNode body = ApiHttp.MAPPER.createObjectNode();
        body.put("model", data.setting().model().getModelId());
        body.put("max_tokens", data.setting().maxTokens());
        body.putArray("messages").addObject()
                .put("role", "user")
                .put("content", data.prompt());

        return ApiHttp.sendJsonRequest(
                "https://api.openai.com/v1/chat/completions",
                Map.of("Authorization", "Bearer " + data.setting().currentApiKey()),
                body,
                root -> root.path("choices").get(0).path("message").path("content").asText().trim(),
                "OpenAI"
        );
    }

}
