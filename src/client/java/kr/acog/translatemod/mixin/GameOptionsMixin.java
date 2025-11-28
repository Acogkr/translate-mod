package kr.acog.translatemod.mixin;

import kr.acog.translatemod.TranslateModClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

    @Shadow
    @Mutable
    public KeyBinding[] allKeys;

    @Inject(method = "load", at = @At("HEAD"))
    private void load(CallbackInfo ci) {
        this.allKeys = ArrayUtils.add(this.allKeys, TranslateModClient.toggleKeyBinding);
    }

}
