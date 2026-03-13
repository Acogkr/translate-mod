package kr.acog.translatemod.type;

import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public enum TargetLanguage {
    KO("translatemod.language.korean", "Korean", "ko"),
    EN("translatemod.language.english", "English", "en"),
    JA("translatemod.language.japanese", "Japanese", "ja"),
    ZH_CN("translatemod.language.zh_cn", "Chinese (Simplified)", "zh_cn"),
    ZH_TW("translatemod.language.zh_tw", "Chinese (Traditional)", "zh_tw"),
    ES("translatemod.language.es", "Spanish", "es"),
    FR("translatemod.language.fr", "French", "fr"),
    DE("translatemod.language.de", "German", "de"),
    RU("translatemod.language.ru", "Russian", "ru"),
    PT_BR("translatemod.language.pt_br", "Portuguese (Brazilian)", "pt_br"),
    IT("translatemod.language.it", "Italian", "it"),
    NL("translatemod.language.nl", "Dutch", "nl"),
    PL("translatemod.language.pl", "Polish", "pl"),
    TR("translatemod.language.tr", "Turkish", "tr"),
    AR("translatemod.language.ar", "Arabic", "ar"),
    VI("translatemod.language.vi", "Vietnamese", "vi"),
    TH("translatemod.language.th", "Thai", "th"),
    ID("translatemod.language.id", "Indonesian", "id"),
    HI("translatemod.language.hi", "Hindi", "hi"),
    SV("translatemod.language.sv", "Swedish", "sv"),
    DA("translatemod.language.da", "Danish", "da"),
    NO("translatemod.language.no", "Norwegian", "no"),
    FI("translatemod.language.fi", "Finnish", "fi"),
    CS("translatemod.language.cs", "Czech", "cs"),
    HU("translatemod.language.hu", "Hungarian", "hu"),
    RO("translatemod.language.ro", "Romanian", "ro"),
    UK("translatemod.language.uk", "Ukrainian", "uk");

    private final String nameKey;
    private final String apiName;
    private final String langPrefix;

    TargetLanguage(String nameKey, String apiName, String langPrefix) {
        this.nameKey = nameKey;
        this.apiName = apiName;
        this.langPrefix = langPrefix;
    }

    public Text getName() {
        return Text.translatable(nameKey);
    }

    public Text getPrefix() {
        return Text.translatable("translatemod.prefix");
    }

    public String getApiName() {
        return apiName;
    }

    private static final TargetLanguage[] BY_PREFIX_LENGTH;

    static {
        BY_PREFIX_LENGTH = Arrays.stream(values())
                .sorted(Comparator.comparingInt(lang -> -lang.langPrefix.length()))
                .toArray(TargetLanguage[]::new);
    }

    public static Optional<TargetLanguage> fromMinecraftCode(String code) {
        String normalized = code.toLowerCase().replace('-', '_');
        return Arrays.stream(BY_PREFIX_LENGTH)
                .filter(lang -> normalized.startsWith(lang.langPrefix))
                .findFirst();
    }

}
