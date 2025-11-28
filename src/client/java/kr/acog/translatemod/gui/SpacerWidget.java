package kr.acog.translatemod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class SpacerWidget extends ClickableWidget {

    public SpacerWidget(int width, int height) {
        super(0, 0, width, height, Text.literal(""));
        this.active = false;
        this.visible = false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

}