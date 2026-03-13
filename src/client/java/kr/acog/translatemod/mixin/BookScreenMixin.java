package kr.acog.translatemod.mixin;

import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.feature.BookTranslateFeature;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookScreen.class)
public abstract class BookScreenMixin extends Screen {

    protected BookScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "addCloseButton", at = @At("HEAD"), cancellable = true)
    private void onAddCloseButton(CallbackInfo ci) {
        BookTranslateFeature.initForScreen((BookScreen) (Object) this);

        if (!ClientSettingManager.getSetting().enabled()) {
            return;
        }

        ci.cancel();

        int buttonY = 196;
        int gap = 4;
        int buttonWidth = (200 - gap) / 2;
        int leftX = width / 2 - 100;

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, btn -> close())
                .dimensions(leftX, buttonY, buttonWidth, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("translatemod.button.translate"),
                btn -> BookTranslateFeature.translateCurrentPage((BookScreen) (Object) this))
                .dimensions(leftX + buttonWidth + gap, buttonY, buttonWidth, 20)
                .build());
    }

}
