package kr.acog.translatemod.config;

import kr.acog.translatemod.type.Model;
import kr.acog.translatemod.type.PromptMode;
import kr.acog.translatemod.type.TargetLanguage;

public record ClientSetting(boolean enabled, String key, PromptMode mode, Model model, String prompt, int maxTokens, TargetLanguage targetLanguage, long suggestionTimeout) {

    public static ClientSetting ofDefault() {
        return new ClientSetting(true, "", PromptMode.STANDARD, Model.GEMINI_2_0_FLASH_LITE, "", 1000, TargetLanguage.EN, 3000L);
    }

}
