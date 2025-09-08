package kr.acog.translatemod.client.config;

import kr.acog.translatemod.api.ProviderType;

public record ClientSetting(ProviderType type, String key, String prompt) {

    public static ClientSetting ofDefault() {
        return new ClientSetting(ProviderType.OPENROUTER, "", "");
    }

}
