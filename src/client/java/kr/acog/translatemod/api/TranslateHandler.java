package kr.acog.translatemod.api;

import kr.acog.translatemod.api.handler.GoogleGeminiRequestHandler;
import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.type.TargetLanguage;

import java.util.concurrent.CompletableFuture;

public class TranslateHandler {

    private static final String basePrompt = "Translate the following text into %s. Return ONLY the translated text. Do not include any explanations, emojis, or markdown formatting. If the text is already in the target language, return it as is. The user's prompt: %s. The content to be translated is: ";

    public static CompletableFuture<String> translateAsync(String original, TargetLanguage targetLanguageCode,
            ClientSetting setting) {
        if (!setting.enabled()) {
            return CompletableFuture.completedFuture(original);
        }

        return CompletableFuture.supplyAsync(() -> {
            String targetLanguageName = targetLanguageCode.getName();
            return TranslateData.ofDefault(setting, original, targetLanguageName);
        }).thenCompose(data -> {
            if (data.setting.key().isEmpty()) {
                return CompletableFuture.completedFuture("API 키가 설정되지 않았습니다.");
            }

            CompletableFuture<String> primary = GoogleGeminiRequestHandler.request(data);

            if (primary == null) {
                return CompletableFuture.completedFuture("요청 생성 실패.");
            }

            return primary.handle((result, ex) -> {
                if (ex != null) {
                    return "번역 실패: " + ex.getMessage();
                }
                return result;
            });
        });
    }

    public record TranslateData(ClientSetting setting, String original, String prompt) {

        public static TranslateData ofDefault(ClientSetting setting, String original, String targetLanguage) {
            return new TranslateData(setting, original, composePrompt(targetLanguage, setting.prompt()));
        }

        private static String composePrompt(String targetLanguage, String prompt) {
            return String.format(basePrompt, targetLanguage, prompt);
        }

    }

}
