package kr.acog.translatemod.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.acog.translatemod.type.Model;
import kr.acog.translatemod.type.TargetLanguage;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ClientSettingManager {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("translatemod_settings.json");

    private static ClientSetting setting = ClientSetting.ofDefault();

    private static final ObjectMapper mapper = new ObjectMapper();

    public static ClientSetting getSetting() {
        return setting;
    }

    public static void setSetting(ClientSetting newSetting) {
        setting = newSetting;
        saveSetting();
    }

    public static void loadSetting() {
        try {
            if (CONFIG_PATH.toFile().exists()) {
                ClientSetting loaded = mapper.readValue(CONFIG_PATH.toFile(), ClientSetting.class);
                setting = new ClientSetting(
                        loaded.enabled(),
                        decode(loaded.key()),
                        loaded.model() == null ? Model.GEMINI_2_0_FLASH_LITE : loaded.model(),
                        loaded.prompt(),
                        loaded.maxTokens() == 0 ? 1000 : loaded.maxTokens(),
                        loaded.outgoingTargetLanguage() == null ? TargetLanguage.EN
                                : loaded.outgoingTargetLanguage(),
                        loaded.suggestionTimeout() == 0 ? 3000L : loaded.suggestionTimeout());
            } else {
                saveSetting();
            }
        } catch (Exception e) {
            e.printStackTrace();
            setting = ClientSetting.ofDefault();
        }
    }

    public static void saveSetting() {
        try {
            ClientSetting encrypted = new ClientSetting(
                    setting.enabled(),
                    encode(setting.key()),
                    setting.model(),
                    setting.prompt(),
                    setting.maxTokens(),
                    setting.outgoingTargetLanguage(),
                    setting.suggestionTimeout());
            mapper.writerWithDefaultPrettyPrinter().writeValue(CONFIG_PATH.toFile(), encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encode(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return java.util.Base64.getEncoder().encodeToString(input.getBytes());
    }

    private static String decode(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        try {
            return new String(java.util.Base64.getDecoder().decode(input));
        } catch (IllegalArgumentException e) {
            return input;
        }
    }

}
