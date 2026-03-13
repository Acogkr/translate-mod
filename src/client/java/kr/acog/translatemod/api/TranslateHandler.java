package kr.acog.translatemod.api;

import kr.acog.translatemod.api.handler.ClaudeRequestHandler;
import kr.acog.translatemod.api.handler.GoogleGeminiRequestHandler;
import kr.acog.translatemod.api.handler.OllamaRequestHandler;
import kr.acog.translatemod.api.handler.OpenAiRequestHandler;
import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.type.Provider;
import kr.acog.translatemod.type.TargetLanguage;

import java.util.concurrent.CompletableFuture;

public class TranslateHandler {

    public static CompletableFuture<String> translateAsync(String original, TargetLanguage targetLanguage, ClientSetting setting) {
        if (!setting.enabled()) {
            return CompletableFuture.completedFuture(original);
        }

        return requestPromptAsync(TranslateData.ofDefault(setting, original, targetLanguage.getApiName()).prompt(), setting);
    }

    public static CompletableFuture<String> requestPromptAsync(String prompt, ClientSetting setting) {
        if (!setting.enabled()) {
            return CompletableFuture.completedFuture(prompt);
        }

        TranslateData data = new TranslateData(setting, prompt);

        if (data.setting().model().getProvider() != Provider.OLLAMA && data.setting().currentApiKey().isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("API 키가 설정되지 않았습니다."));
        }

        return dispatch(data);
    }

    private static CompletableFuture<String> dispatch(TranslateData data) {
        return switch (data.setting().model().getProvider()) {
            case GEMINI -> GoogleGeminiRequestHandler.request(data);
            case OPENAI -> OpenAiRequestHandler.request(data);
            case CLAUDE -> ClaudeRequestHandler.request(data);
            case OLLAMA -> OllamaRequestHandler.request(data);
        };
    }

    public record TranslateData(ClientSetting setting, String prompt) {

        public static TranslateData ofDefault(ClientSetting setting, String original, String targetLanguage) {
            return new TranslateData(setting, setting.mode().format(targetLanguage, setting.prompt(), original));
        }

    }

}
