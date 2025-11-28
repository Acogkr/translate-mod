package kr.acog.translatemod.type;

public enum TargetLanguage {
    KO("Korean"),
    EN("English"),
    JP("Japanese");

    private final String name;

    TargetLanguage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return switch (this) {
            case KO -> "[번역] ";
            case EN -> "[Translate] ";
            case JP -> "[翻訳] ";
        };
    }
}
