package kr.acog.translatemod.api;

public enum ProviderType {
    CHATGPT("ChatGPT"),
    GOOGLE_GEMINI("Google Gemini"),
    OPENROUTER("OpenRouter"),;

    private final String name;

    ProviderType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
