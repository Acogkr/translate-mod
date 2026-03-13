package kr.acog.translatemod.type;

import java.util.Arrays;
import java.util.List;

public enum Model {
    GEMINI_2_5_PRO("gemini-2.5-pro", Provider.GEMINI, true),
    GEMINI_2_5_FLASH("gemini-2.5-flash", Provider.GEMINI, false),
    GEMINI_2_5_FLASH_LITE("gemini-2.5-flash-lite", Provider.GEMINI, false),
    GEMINI_2_0_FLASH("gemini-2.0-flash", Provider.GEMINI, false),
    GEMINI_2_0_FLASH_LITE("gemini-2.0-flash-lite", Provider.GEMINI, false),

    GPT_4O("gpt-4o", Provider.OPENAI, true),
    GPT_4O_MINI("gpt-4o-mini", Provider.OPENAI, false),
    GPT_4_TURBO("gpt-4-turbo", Provider.OPENAI, true),
    GPT_3_5_TURBO("gpt-3.5-turbo", Provider.OPENAI, false),

    CLAUDE_OPUS_4_6("claude-opus-4-6", Provider.CLAUDE, true),
    CLAUDE_SONNET_4_6("claude-sonnet-4-6", Provider.CLAUDE, true),
    CLAUDE_HAIKU_4_5("claude-haiku-4-5-20251001", Provider.CLAUDE, false),

    OLLAMA_CUSTOM("ollama", Provider.OLLAMA, false);

    private final String modelId;
    private final Provider provider;
    private final boolean expensive;

    Model(String modelId, Provider provider, boolean expensive) {
        this.modelId = modelId;
        this.provider = provider;
        this.expensive = expensive;
    }

    public String getModelId() {
        return modelId;
    }

    public Provider getProvider() {
        return provider;
    }

    public boolean isExpensive() {
        return expensive;
    }

    public static List<Model> forProvider(Provider provider) {
        return Arrays.stream(values())
                .filter(m -> m.provider == provider)
                .toList();
    }
}
