package kr.acog.translatemod.type;

public enum PromptMode {

    ECONOMY("절약 모드", """
            Context: Minecraft Chat
            Target Language: %s
            Command: Translate the text below.
            Strict Constraint: Output ONLY the translated string. Do not use quotation marks.
            
            Input:
            """),

    STANDARD("표준 모드", """
            Context: Minecraft Chat
            Target Language: %s
            User Rule: %s
            
            Command: Translate the text below.
            Strict Constraint: Output ONLY the translated string. Do not use quotation marks.
            
            1. Translate the input text naturally, keeping gaming nuances.
            
            Input:
            """),

    PRECISE("정밀 모드", """
            Context: Minecraft Chat
            Target Language: %s
            User Rule: %s
            
            Command: Translate the text below.
            Strict Constraint: Output ONLY the translated string. Do not use quotation marks.
            
            1. Translate the input text naturally, keeping gaming nuances.
            2. DO NOT include any introductory text (e.g., "Sure", "Here is").
            3. DO NOT wrap the result in quotes ("") or markdown code blocks.
            4. Return ONLY the raw translated string.
            
            input:
            """);

    private final String label;
    private final String template;

    PromptMode(String label, String template) {
        this.label = label;
        this.template = template;
    }

    public String getLabel() {
        return label;
    }

    public String format(String targetLang, String userRule, String originalText) {
        String instruction;

        if (this == ECONOMY) {
            instruction = String.format(template, targetLang);
        } else {
            String safeRule = (userRule == null || userRule.isBlank()) ? "Translate naturally" : userRule;
            instruction = String.format(template, targetLang, safeRule);
        }

        // 지시문 뒤에 원본 텍스트를 붙여서 반환
        return instruction + originalText;
    }
}