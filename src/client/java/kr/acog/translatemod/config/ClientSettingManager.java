package kr.acog.translatemod.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.acog.translatemod.type.Provider;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class ClientSettingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("translatemod");
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("translatemod_settings.json");
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ClientSetting DEFAULTS = ClientSetting.ofDefault();

    private static volatile ClientSetting setting = DEFAULTS;

    public static ClientSetting getSetting() {
        return setting;
    }

    public static void setSetting(ClientSetting newSetting) {
        setting = newSetting;
        saveSetting();
    }

    public static void setSettingWithoutSave(ClientSetting newSetting) {
        setting = newSetting;
    }

    public static void loadSetting() {
        try {
            if (CONFIG_PATH.toFile().exists()) {
                ClientSetting loaded = mapper.readValue(CONFIG_PATH.toFile(), ClientSetting.class);
                setting = new ClientSetting(
                        loaded.suggestionLanguage() == null ? DEFAULTS.suggestionLanguage() : loaded.suggestionLanguage(),
                        loaded.suggestionTimeout() == 0 ? DEFAULTS.suggestionTimeout() : loaded.suggestionTimeout(),
                        loaded.targetLanguage() == null ? DEFAULTS.targetLanguage() : loaded.targetLanguage(),
                        loaded.customModelId() == null || loaded.customModelId().isBlank() ? DEFAULTS.customModelId() : loaded.customModelId(),
                        loaded.maxTokens() == 0 ? DEFAULTS.maxTokens() : loaded.maxTokens(),
                        transformKeys(loaded.apiKeys(), EncryptionUtil::decrypt),
                        loaded.baseUrl() == null || loaded.baseUrl().isBlank() ? DEFAULTS.baseUrl() : loaded.baseUrl(),
                        loaded.enabled(),
                        loaded.prompt() == null ? DEFAULTS.prompt() : loaded.prompt(),
                        loaded.model() == null ? DEFAULTS.model() : loaded.model(),
                        loaded.mode() == null ? DEFAULTS.mode() : loaded.mode(),
                        loaded.scope() == null ? DEFAULTS.scope() : loaded.scope()
                );
            } else {
                saveSetting();
            }
        } catch (Exception e) {
            LOGGER.error("설정 로드 실패, 기본값 사용", e);
            setting = DEFAULTS;
        }
    }

    public static void saveSetting() {
        try {
            ClientSetting toSave = new ClientSetting(
                    setting.suggestionLanguage(),
                    setting.suggestionTimeout(),
                    setting.targetLanguage(),
                    setting.customModelId(),
                    setting.maxTokens(),
                    transformKeys(setting.apiKeys(), EncryptionUtil::encrypt),
                    setting.baseUrl(),
                    setting.enabled(),
                    setting.prompt(),
                    setting.model(),
                    setting.mode(),
                    setting.scope()
            );
            mapper.writerWithDefaultPrettyPrinter().writeValue(CONFIG_PATH.toFile(), toSave);
        } catch (Exception e) {
            LOGGER.error("설정 저장 실패", e);
        }
    }

    private static Map<Provider, String> transformKeys(Map<Provider, String> keys, UnaryOperator<String> transformer) {
        Map<Provider, String> result = new HashMap<>();
        if (keys != null) {
            keys.forEach((provider, key) -> {
                if (key != null && !key.isEmpty()) {
                    String transformed = transformer.apply(key);
                    if (transformed != null) {
                        result.put(provider, transformed);
                    }
                }
            });
        }
        return result;
    }

}
