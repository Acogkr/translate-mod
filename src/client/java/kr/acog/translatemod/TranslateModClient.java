package kr.acog.translatemod;

import kr.acog.translatemod.config.ClientSettingManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class TranslateModClient implements ClientModInitializer {

    public static KeyBinding toggleKeyBinding;

    @Override
    public void onInitializeClient() {
        ClientSettingManager.loadSetting();

        toggleKeyBinding = new KeyBinding(
                "번역 활성화/비활성화",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyBinding.Category.MULTIPLAYER);
    }

}
