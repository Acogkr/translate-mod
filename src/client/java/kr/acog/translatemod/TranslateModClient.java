package kr.acog.translatemod;

import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.feature.ChatTranslateFeature;
import kr.acog.translatemod.feature.SignTranslateFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class TranslateModClient implements ClientModInitializer {

    public static KeyBinding toggleKeyBinding;

    @Override
    public void onInitializeClient() {
        ClientSettingManager.loadSetting();
        registerToggleKeyBinding();
        ChatTranslateFeature.register();
        SignTranslateFeature.register();
        // BookTranslateFeature is injected via BookScreenMixin — no register() needed
    }

    private void registerToggleKeyBinding() {
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "translatemod.option.key_bind",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyBinding.Category.MULTIPLAYER));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKeyBinding.wasPressed()) {
                ClientSetting setting = ClientSettingManager.getSetting();
                boolean next = !setting.enabled();
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.translatable(next ? "translatemod.option.enabled" : "translatemod.option.disabled"),
                            false);
                }
                ClientSettingManager.setSetting(setting.withEnabled(next));
            }
        });
    }

}
