package kr.acog.translatemod.client.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    // All logic has been moved to MessageHandlerMixin to handle the full, original message.
}
