package kr.acog.translatemod.config;

import kr.acog.translatemod.type.Model;
import kr.acog.translatemod.type.PromptMode;
import kr.acog.translatemod.type.Provider;
import kr.acog.translatemod.type.TargetLanguage;
import kr.acog.translatemod.type.TranslateScope;

import java.util.HashMap;
import java.util.Map;

public record ClientSetting(
        TargetLanguage suggestionLanguage,
        long suggestionTimeout,
        TargetLanguage targetLanguage,
        String customModelId,
        int maxTokens,
        Map<Provider, String> apiKeys,
        String baseUrl,
        boolean enabled,
        String prompt,
        Model model,
        PromptMode mode,
        TranslateScope scope
) {

    public String currentApiKey() {
        if (apiKeys == null) {
            return "";
        }
        return apiKeys.getOrDefault(model.getProvider(), "");
    }

    public ClientSetting withSuggestionLanguage(TargetLanguage value) {
        return new ClientSetting(value, suggestionTimeout, targetLanguage, customModelId,
                maxTokens, apiKeys, baseUrl, enabled, prompt, model, mode, scope);
    }

    public ClientSetting withSuggestionTimeout(long value) {
        return new ClientSetting(suggestionLanguage, value, targetLanguage, customModelId,
                maxTokens, apiKeys, baseUrl, enabled, prompt, model, mode, scope);
    }

    public ClientSetting withTargetLanguage(TargetLanguage value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, value, customModelId,
                maxTokens, apiKeys, baseUrl, enabled, prompt, model, mode, scope);
    }

    public ClientSetting withCustomModelId(String value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, targetLanguage, value,
                maxTokens, apiKeys, baseUrl, enabled, prompt, model, mode, scope);
    }

    public ClientSetting withMaxTokens(int value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, targetLanguage, customModelId,
                value, apiKeys, baseUrl, enabled, prompt, model, mode, scope);
    }

    public ClientSetting withApiKeys(Map<Provider, String> value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, targetLanguage, customModelId,
                maxTokens, value, baseUrl, enabled, prompt, model, mode, scope);
    }

    public ClientSetting withBaseUrl(String value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, targetLanguage, customModelId,
                maxTokens, apiKeys, value, enabled, prompt, model, mode, scope);
    }

    public ClientSetting withEnabled(boolean value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, targetLanguage, customModelId,
                maxTokens, apiKeys, baseUrl, value, prompt, model, mode, scope);
    }

    public ClientSetting withPrompt(String value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, targetLanguage, customModelId,
                maxTokens, apiKeys, baseUrl, enabled, value, model, mode, scope);
    }

    public ClientSetting withModel(Model value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, targetLanguage, customModelId,
                maxTokens, apiKeys, baseUrl, enabled, prompt, value, mode, scope);
    }

    public ClientSetting withMode(PromptMode value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, targetLanguage, customModelId,
                maxTokens, apiKeys, baseUrl, enabled, prompt, model, value, scope);
    }

    public ClientSetting withScope(TranslateScope value) {
        return new ClientSetting(suggestionLanguage, suggestionTimeout, targetLanguage, customModelId,
                maxTokens, apiKeys, baseUrl, enabled, prompt, model, mode, value);
    }

    public String translationFingerprint() {
        String modelKey = model.getProvider() == Provider.OLLAMA ? customModelId : model.getModelId();
        return modelKey + "\0" + mode.name() + "\0" + (prompt == null ? "" : prompt);
    }

    public static ClientSetting ofDefault() {
        return new ClientSetting(
                TargetLanguage.EN, 3000L, TargetLanguage.KO, "llama3", 1000,
                new HashMap<>(), "http://localhost:11434", true, "",
                Model.GEMINI_2_0_FLASH_LITE, PromptMode.STANDARD, TranslateScope.ALL
        );
    }

}
