package kr.acog.translatemod.type;

import net.minecraft.text.Text;

public enum TranslateScope {
    ALL("translatemod.scope.all"),
    CHAT_ONLY("translatemod.scope.chat_only");

    private final String labelKey;

    TranslateScope(String labelKey) {
        this.labelKey = labelKey;
    }

    public Text getLabel() {
        return Text.translatable(labelKey);
    }
}
