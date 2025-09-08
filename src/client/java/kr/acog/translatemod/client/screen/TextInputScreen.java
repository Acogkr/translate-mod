package kr.acog.translatemod.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class TextInputScreen extends Screen {

    private final Screen parent;
    private final boolean masked;
    private final String initialText;
    private final Consumer<String> callback;
    private TextFieldWidget textField;

    public TextInputScreen(String title, String initText, Screen parent, boolean maskInput, Consumer<String> callback) {
        super(Text.literal(title));
        this.initialText = initText;
        this.parent = parent;
        this.masked = maskInput;
        this.callback = callback;
    }

    @Override
    protected void init() {
        super.init();
        this.textField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 100,
                this.height / 2 - 10,
                200,
                20,
                null
        );

        textField.setMaxLength(100);
        if (masked) {
            textField.setText("*".repeat(initialText.length()));

            textField.setRenderTextProvider((rawText, cursorPos) -> {
                String masked = "*".repeat(rawText.length());
                return Text.literal(masked).asOrderedText();
            });
        } else {
            textField.setText(initialText);
        }

        this.addDrawableChild(this.textField);
        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Done"), button -> {
                    callback.accept(this.textField.getText());
                    this.close();
                }).dimensions(
                    this.width / 2 - 100,
                    this.height / 2 + 20,
                    200,
                    20
                ).build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                this.height / 2 - 40,
                0xFFFFFF
        );

    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

}
