package kr.acog.translatemod.type;

public enum Model {
    GEMINI_2_5_PRO("gemini-2.5-pro"),
    GEMINI_2_5_FLASH("gemini-2.5-flash"),
    GEMINI_2_5_FLASH_LITE("gemini-2.5-flash-lite"),
    GEMINI_2_0_FLASH("gemini-2.0-flash"),
    GEMINI_2_0_FLASH_LITE("gemini-2.0-flash-lite");

    private final String modelId;

    Model(String modelId) {
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }
}
