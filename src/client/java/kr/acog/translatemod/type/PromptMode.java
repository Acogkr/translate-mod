package kr.acog.translatemod.type;

import net.minecraft.text.Text;

public enum PromptMode {

    ECONOMY("translatemod.prompt.economy", """
            Context: Minecraft Chat
            Target Language: %s
            Command: Translate the text below.
            Strict Constraint: Output ONLY the translated string. Do not use quotation marks.
            Translate only the chat message, excluding nicknames and titles.

            Input:
            """),

    STANDARD("translatemod.prompt.standard", """
            Context: Minecraft Chat
            Target Language: %s
            User Rule: %s

            Command: Translate the text below.
            Strict Constraint: Output ONLY the translated string. Do not use quotation marks.
            Translate only the chat message, excluding nicknames and titles.

            1. Translate the input text naturally, keeping gaming nuances.

            Input:
            """),

    PRECISE("translatemod.prompt.precise", """
            Context: Minecraft Chat
            Target Language: %s
            User Rule: %s

            Command: Translate the text below.
            Strict Constraint: Output ONLY the translated string. Do not use quotation marks.
            Translate only the chat message, excluding nicknames and titles.

            1. Translate the input text naturally, keeping gaming nuances.
            2. DO NOT include any introductory text (e.g., "Sure", "Here is").
            3. DO NOT wrap the result in quotes ("") or markdown code blocks.
            4. Return ONLY the raw translated string.

            input:
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
        String instruction;

        if (this == ECONOMY) {
            instruction = String.format(template, targetLang);
        } else {
            String safeRule = (userRule == null || userRule.isBlank()) ? "Translate naturally" : userRule;
            instruction = String.format(template, targetLang, safeRule);
        }

        return instruction + originalText;
    }
}