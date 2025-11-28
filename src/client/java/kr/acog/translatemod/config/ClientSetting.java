package kr.acog.translatemod.config;

import kr.acog.translatemod.type.Model;
import kr.acog.translatemod.type.TargetLanguage;

public record ClientSetting(boolean enabled, String key, Model model, String prompt, int maxTokens,
        TargetLanguage outgoingTargetLanguage, long suggestionTimeout) {

    public static ClientSetting ofDefault() {
        return new ClientSetting(true, "", Model.GEMINI_2_0_FLASH_LITE, "", 1000, TargetLanguage.EN, 3000L);
    }

}
