package kr.acog.translatemod.type;

import java.util.Arrays;
import java.util.List;

public enum Model {
    GEMINI_3_1_PRO("gemini-3.1-pro-preview", Provider.GEMINI, true),
    GEMINI_3_5_FLASH("gemini-3.5-flash", Provider.GEMINI, false),
    GEMINI_3_1_FLASH_LITE("gemini-3.1-flash-lite", Provider.GEMINI, false),
    GEMINI_2_5_FLASH("gemini-2.5-flash", Provider.GEMINI, false),
    GEMINI_2_5_FLASH_LITE("gemini-2.5-flash-lite", Provider.GEMINI, false),

    GPT_5_5("gpt-5.5", Provider.OPENAI, true),
    GPT_5_4_MINI("gpt-5.4-mini", Provider.OPENAI, false),
    GPT_5_MINI("gpt-5-mini", Provider.OPENAI, false),
    GPT_5_NANO("gpt-5-nano", Provider.OPENAI, false),

    CLAUDE_OPUS_4_8("claude-opus-4-8", Provider.CLAUDE, true),
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
