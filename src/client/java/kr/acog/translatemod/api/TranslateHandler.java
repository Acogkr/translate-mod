package kr.acog.translatemod.api;

import kr.acog.translatemod.api.handler.ChatGPTRequestHandler;
import kr.acog.translatemod.api.handler.GoogleGeminiRequestHandler;
import kr.acog.translatemod.api.handler.OpenRouterRequestHandler;
import kr.acog.translatemod.client.config.ClientSetting;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class TranslateHandler {

    private static final String basePrompt = "Extract only the chat portion and translate it into %s language, excluding difficult or context-dependent terms. Return only the translated text. The rest is the user’s prompt: %s. The content to be translated is: ";

    public static CompletableFuture<String> translateAsync(String original, Locale from, ClientSetting setting) {
        TranslateData data = TranslateData.ofDefault(setting, original, from);

        if (data.setting.key().isEmpty()) {
            return CompletableFuture.completedFuture("API key is not set.");
        }

        switch (data.setting.type()) {
            case CHATGPT -> {
                return ChatGPTRequestHandler.request(data);
            }
            case GOOGLE_GEMINI -> {
                return GoogleGeminiRequestHandler.request(data);
            }
            case OPENROUTER -> {
                return OpenRouterRequestHandler.request(data);
            }
            default -> {
                throw new IllegalArgumentException("Unsupported provider type: " + data.setting.type());
            }
        }

    }

    public record TranslateData(ClientSetting setting,String original, String prompt) {

        public static TranslateData ofDefault(ClientSetting setting, String original, Locale from) {
            return new TranslateData(setting, original, composePrompt(from, setting.prompt()));
        }

        private static String composePrompt(Locale from, String prompt) {
            return String.format(basePrompt, from.getLanguage(), prompt);
        }

    }

}
