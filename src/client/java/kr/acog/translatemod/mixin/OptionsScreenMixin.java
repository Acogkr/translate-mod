package kr.acog.translatemod.mixin;

import kr.acog.translatemod.gui.SpacerWidget;
import kr.acog.translatemod.gui.TranslateOptionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {

    @Shadow
    protected abstract ButtonWidget createButton(Text message, Supplier<Screen> screenSupplier);

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;createAdder(I)Lnet/minecraft/client/gui/widget/GridWidget$Adder;", ordinal = 0))
    private GridWidget.Adder onCreateAdder(GridWidget gridWidget, int columns) {
        Text title = Text.translatable("translatemod.title.options");
        GridWidget.Adder adder = gridWidget.createAdder(columns);
        adder.add(this.createButton(title, TranslateOptionScreen::new));
        adder.add(new SpacerWidget(150, 20));
        return adder;
    }

}