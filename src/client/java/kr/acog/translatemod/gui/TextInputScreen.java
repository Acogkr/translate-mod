package kr.acog.translatemod.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class TextInputScreen extends Screen {

    private final String titleText;
    private final String initialValue;
    private final Screen parent;
    private final boolean isPassword;
    private final Consumer<String> onSave;
    private TextFieldWidget textField;

    public TextInputScreen(String title, String initialValue, Screen parent, boolean isPassword, Consumer<String> onSave) {
        super(Text.literal(title));
        this.titleText = title;
        this.initialValue = initialValue;
        this.parent = parent;
        this.isPassword = isPassword;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        textField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 20, 200, 20, Text.literal(""));
        textField.setMaxLength(1024);
        textField.setText(initialValue);

        if (isPassword) {
            textField.addFormatter(new MaskingFormatter(true));
        }

        this.addDrawableChild(textField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("저장"), button -> {
            onSave.accept(textField.getText());
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX - 105, centerY + 10, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("취소"), button -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX + 5, centerY + 10, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(this.titleText), this.width / 2,
                this.height / 2 - 50, 0xFFFFFF);
    }

}
