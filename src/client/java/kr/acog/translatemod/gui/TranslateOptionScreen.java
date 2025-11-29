package kr.acog.translatemod.gui;

import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.type.Model;
import kr.acog.translatemod.type.PromptMode;
import kr.acog.translatemod.type.TargetLanguage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Supplier;

public class TranslateOptionScreen extends Screen {

    private ClientSetting setting;
    private final Model[] models = Model.values();
    private int currentModelIndex = 0;
    private ButtonWidget modelButton;

    public TranslateOptionScreen() {
        super(Text.translatable("translatemod.title.options"));
    }

    @Override
    protected void init() {
        super.init();
        setting = ClientSettingManager.getSetting();
        currentModelIndex = getModelIndex(setting.model());

        int btnWidth = 150;
        int btnHeight = 20;
        int spacing = 5;
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int leftX = centerX - btnWidth - 5;
        int rightX = centerX + 5;

        this.addDrawableChild(createButton(Text.translatable(setting.enabled() ? "translatemod.option.enabled" : "translatemod.option.disabled"), btn -> {
            boolean newState = !setting.enabled();
            updateSetting(newState, setting.key(), setting.mode(), setting.model(), setting.prompt(), setting.maxTokens(), setting.targetLanguage(), setting.suggestionTimeout());
            btn.setMessage(Text.translatable(newState ? "translatemod.option.enabled" : "translatemod.option.disabled"));
        }, leftX, startY, btnWidth, btnHeight));

        this.addDrawableChild(createButton(Text.translatable("translatemod.option.api_key"), () -> new TextInputScreen(Text.translatable("translatemod.screen.api_key.title"), setting.key(), this, true, result -> {
            updateSetting(setting.enabled(), result, setting.mode(), setting.model(), setting.prompt(), setting.maxTokens(), setting.targetLanguage(), setting.suggestionTimeout());
        }), rightX, startY, btnWidth, btnHeight));

        modelButton = createButton(Text.translatable("translatemod.option.model", models[currentModelIndex].getModelId()), btn -> {
            currentModelIndex = (currentModelIndex + 1) % models.length;
            updateModelButtonState();
            updateSetting(setting.enabled(), setting.key(), setting.mode(), models[currentModelIndex], setting.prompt(), setting.maxTokens(), setting.targetLanguage(), setting.suggestionTimeout());
        }, leftX, startY + btnHeight + spacing, btnWidth, btnHeight);
        updateModelButtonState();
        this.addDrawableChild(modelButton);

        this.addDrawableChild(createButton(Text.translatable("translatemod.option.prompt_settings"), () -> new TextInputScreen(Text.translatable("translatemod.screen.prompt.title"), setting.prompt(), this, false, result -> {
            updateSetting(setting.enabled(), setting.key(), setting.mode(), setting.model(), result, setting.maxTokens(), setting.targetLanguage(), setting.suggestionTimeout());
        }), rightX, startY + btnHeight + spacing, btnWidth, btnHeight));

        this.addDrawableChild(createButton(Text.translatable("translatemod.option.target_language", setting.targetLanguage().getName().getString()), btn -> {
            TargetLanguage next = getNextLanguage(setting.targetLanguage());
            updateSetting(setting.enabled(), setting.key(), setting.mode(), setting.model(), setting.prompt(), setting.maxTokens(), next, setting.suggestionTimeout());
            btn.setMessage(Text.translatable("translatemod.option.target_language", next.getName().getString()));
        }, leftX, startY + (btnHeight + spacing) * 2, btnWidth, btnHeight));

        this.addDrawableChild(createButton(Text.translatable("translatemod.option.prompt_mode", setting.mode().getLabel().getString()), btn -> {
            PromptMode next = getNextPromptMode(setting.mode());
            updateSetting(setting.enabled(), setting.key(), next, setting.model(), setting.prompt(), setting.maxTokens(), setting.targetLanguage(), setting.suggestionTimeout());
            btn.setMessage(Text.translatable("translatemod.option.prompt_mode", next.getLabel().getString()));
        }, rightX, startY + (btnHeight + spacing) * 2, btnWidth, btnHeight));

        this.addDrawableChild(new TokenSlider(centerX - btnWidth / 2, startY + (btnHeight + spacing) * 3 + 10, btnWidth, btnHeight, setting.maxTokens()));

        this.addDrawableChild(new TimeoutSlider(centerX - btnWidth / 2, startY + (btnHeight + spacing) * 4 + 10, btnWidth, btnHeight, setting.suggestionTimeout()));
    }

    private void updateModelButtonState() {
        Model current = models[currentModelIndex];
        boolean isWarning = current != Model.GEMINI_2_0_FLASH_LITE;

        Text message = Text.translatable("translatemod.option.model", current.getModelId());
        if (isWarning) {
            message = message.copy().formatted(Formatting.RED);
            modelButton.setTooltip(Tooltip.of(Text.translatable("translatemod.warning.model_cost").formatted(Formatting.RED)));
        } else {
            modelButton.setTooltip(null);
        }
        modelButton.setMessage(message);
    }

    private int getModelIndex(Model model) {
        for (int i = 0; i < models.length; i++) {
            if (models[i] == model) {
                return i;
            }
        }
        return 0;
    }

    private void updateSetting(boolean enabled, String key, PromptMode mode, Model model, String prompt, int maxTokens, TargetLanguage outgoing, long suggestionTimeout) {
        setting = new ClientSetting(enabled, key, mode, model, prompt, maxTokens, outgoing, suggestionTimeout);
        ClientSettingManager.setSetting(setting);
    }

    private TargetLanguage getNextLanguage(TargetLanguage current) {
        return switch (current) {
            case TargetLanguage.KO -> TargetLanguage.EN;
            case TargetLanguage.EN -> TargetLanguage.JA;
            default -> TargetLanguage.KO;
        };
    }

    private PromptMode getNextPromptMode(PromptMode current) {
        return switch (current) {
            case PromptMode.ECONOMY -> PromptMode.STANDARD;
            case PromptMode.STANDARD -> PromptMode.PRECISE;
            default -> PromptMode.ECONOMY;
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 4 - 50, 0xFFFFFF);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }

    private ButtonWidget createButton(Text text, Supplier<Screen> screenSupplier, int x, int y, int width, int height) {
        return createButton(text, button -> MinecraftClient.getInstance().setScreen(screenSupplier.get()), x, y, width, height);
    }

    private ButtonWidget createButton(Text text, ButtonWidget.PressAction onPress, int x, int y, int width, int height) {
        return ButtonWidget.builder(text, onPress).dimensions(x, y, width, height).build();
    }

    private class TokenSlider extends SliderWidget {

        public TokenSlider(int x, int y, int width, int height, int initialValue) {
            super(x, y, width, height, Text.translatable("translatemod.option.max_tokens", initialValue), (double) (initialValue - 100) / 4900);
        }

        @Override
        protected void updateMessage() {
            int value = (int) (this.value * 4900) + 100;
            this.setMessage(Text.translatable("translatemod.option.max_tokens", value));
        }

        @Override
        protected void applyValue() {
            int value = (int) (this.value * 4900) + 100;
            updateSetting(setting.enabled(), setting.key(), setting.mode(), setting.model(), setting.prompt(), value, setting.targetLanguage(), setting.suggestionTimeout());
        }

    }

    private class TimeoutSlider extends SliderWidget {

        public TimeoutSlider(int x, int y, int width, int height, long initialValue) {
            super(x, y, width, height, Text.translatable("translatemod.option.suggestion_delay", initialValue), (double) (initialValue - 1000) / 9000);
        }

        @Override
        protected void updateMessage() {
            long value = (long) (this.value * 9000) + 1000;
            this.setMessage(Text.translatable("translatemod.option.suggestion_delay", value));
        }

        @Override
        protected void applyValue() {
            long value = (long) (this.value * 9000) + 1000;
            updateSetting(setting.enabled(), setting.key(), setting.mode(), setting.model(), setting.prompt(), setting.maxTokens(), setting.targetLanguage(), value);
        }

    }

}
