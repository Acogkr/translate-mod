package kr.acog.translatemod.client.config;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ClientSettingManager {

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("translatemod_settings.json");

    private static ClientSetting setting = ClientSetting.ofDefault();

    public static ClientSetting getSetting() {
        return setting;
    }

    public static void setSetting(ClientSetting newSetting) {
        setting = newSetting;
        saveSetting();
    }

    public static void loadSetting() {

    }

    public static void saveSetting() {

    }

}
