package kr.acog.translatemod.type;

import net.minecraft.text.Text;

public enum TargetLanguage {
    KO("translatemod.language.korean", "translatemod.prefix", "Korean"),
    EN("translatemod.language.english", "translatemod.prefix", "English"),
    JA("translatemod.language.japanese", "translatemod.prefix", "Japanese");

    private final String nameKey;
    private final String prefixKey;
    private final String apiName;

    TargetLanguage(String nameKey, String prefixKey, String apiName) {
        this.nameKey = nameKey;
        this.prefixKey = prefixKey;
        this.apiName = apiName;
    }

    public Text getName() {
        return Text.translatable(nameKey);
    }

    public Text getPrefix() {
        return Text.translatable(prefixKey);
    }

    public String getApiName() {
        return apiName;
    }
}
