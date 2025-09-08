package kr.acog.translatemod.client.mixin;

import kr.acog.translatemod.client.access.TickingSuggestorAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void translateMod$onClientTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.currentScreen instanceof ChatScreen chatScreen) {
            ChatScreenAccessor accessor = (ChatScreenAccessor) chatScreen;
            ChatInputSuggestor suggestor = accessor.getChatInputSuggestor();

            if (suggestor instanceof TickingSuggestorAccessor tick) {
                tick.translateMod$tick();
            }
        }
    }

}
