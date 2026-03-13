package kr.acog.translatemod.type;

import net.minecraft.text.Text;

public enum PromptMode {

    ECONOMY("translatemod.prompt.economy", """
            Target Language: %s
            Translate the chat message below. Output ONLY the translated text, nothing else.

            Input:
            """),

    STANDARD("translatemod.prompt.standard", """
            Context: Minecraft chat message
            Target Language: %s
            User Rule: %s

            Translate the chat message below. Output ONLY the translated text.
            Preserve gaming terms, emotes, and player names as-is.

            Input:
            """),

    PRECISE("translatemod.prompt.precise", """
            Context: Minecraft chat message
            Target Language: %s
            User Rule: %s

            Translate the chat message below. Output ONLY the translated text.
            Rules:
            - Preserve gaming terms, emotes, and player names exactly.
            - Never add introductory phrases (e.g. "Sure,", "Here:").
            - Never wrap output in quotes or markdown.
            - Preserve line breaks and formatting from the original.

            Input:
            """);

    private final String labelKey;
    private final String template;

    PromptMode(String labelKey, String template) {
        this.labelKey = labelKey;
        this.template = template;
    }

    public Text getLabel() {
        return Text.translatable(labelKey);
    }

    public String format(String targetLang, String userRule, String originalText) {
        String safeRule = (userRule == null || userRule.isBlank()) ? "Translate naturally" : userRule;
        return String.format(template, targetLang, safeRule) + originalText;
    }

}
