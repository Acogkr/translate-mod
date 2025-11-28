package kr.acog.translatemod.mixin;

import kr.acog.translatemod.access.TickingSuggesterAccessor;
import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import kr.acog.translatemod.TranslateModClient;
import net.minecraft.text.Text;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void translateMod$onClientTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        while (TranslateModClient.toggleKeyBinding.wasPressed()) {
            ClientSetting setting = ClientSettingManager
                    .getSetting();
            boolean newState = !setting.enabled();

            if (client.player != null) {
                client.player.sendMessage(Text.literal(newState ? "번역 모드: 켜짐" : "번역 모드: 꺼짐"), false);
            }

            ClientSettingManager.setSetting(new ClientSetting(
                    newState,
                    setting.key(),
                    setting.model(),
                    setting.prompt(),
                    setting.maxTokens(),
                    setting.outgoingTargetLanguage(),
                    3000L
            ));
        }

        if (client.currentScreen instanceof ChatScreen chatScreen) {
            ChatScreenAccessor accessor = (ChatScreenAccessor) chatScreen;
            ChatInputSuggestor suggestor = accessor.getChatInputSuggestor();

            if (suggestor instanceof TickingSuggesterAccessor tick) {
                tick.translateMod$tick();
            }
        }
    }

}
