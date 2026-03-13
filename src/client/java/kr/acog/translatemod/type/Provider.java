package kr.acog.translatemod.type;

import net.minecraft.text.Text;

public enum Provider {
    GEMINI("translatemod.provider.gemini"),
    OPENAI("translatemod.provider.openai"),
    CLAUDE("translatemod.provider.claude"),
    OLLAMA("translatemod.provider.ollama");

    private final String labelKey;

    Provider(String labelKey) {
        this.labelKey = labelKey;
    }

    public Text getLabel() {
        return Text.translatable(labelKey);
    }
}
