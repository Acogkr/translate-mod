package kr.acog.translatemod.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class TranslateMixin {

    @Inject(at = @At(value = "HEAD"), method = "loadWorld")
    private void init(CallbackInfo info) {
    }

}
