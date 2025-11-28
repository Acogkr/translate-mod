package kr.acog.translatemod.gui;

import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.type.Model;
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
        super(Text.literal("번역 모드 설정"));
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

        // Left Column
        int leftX = centerX - btnWidth - 5;
        // Right Column
        int rightX = centerX + 5;

        // Row 1: Toggle & API Key
        this.addDrawableChild(createButton(setting.enabled() ? "번역 모드: 켜짐" : "번역 모드: 꺼짐", btn -> {
            boolean newState = !setting.enabled();
            updateSetting(newState, setting.key(), setting.model(), setting.prompt(), setting.maxTokens(),
                    setting.outgoingTargetLanguage(), setting.suggestionTimeout());
            btn.setMessage(Text.literal(newState ? "번역 모드: 켜짐" : "번역 모드: 꺼짐"));
        }, leftX, startY, btnWidth, btnHeight));

        this.addDrawableChild(createButton("API 키 설정",
                () -> new TextInputScreen("API 키 입력", setting.key(), this, true, result -> {
                    updateSetting(setting.enabled(), result, setting.model(), setting.prompt(),
                            setting.maxTokens(), setting.outgoingTargetLanguage(), setting.suggestionTimeout());
                }), rightX, startY, btnWidth, btnHeight));

        // Row 2: Model & Prompt
        modelButton = createButton("모델: " + models[currentModelIndex].getModelId(), btn -> {
            currentModelIndex = (currentModelIndex + 1) % models.length;
            updateModelButtonState();
            updateSetting(setting.enabled(), setting.key(), models[currentModelIndex], setting.prompt(),
                    setting.maxTokens(), setting.outgoingTargetLanguage(), setting.suggestionTimeout());
        }, leftX, startY + btnHeight + spacing, btnWidth, btnHeight);
        updateModelButtonState();
        this.addDrawableChild(modelButton);

        this.addDrawableChild(createButton("프롬프트 설정",
                () -> new TextInputScreen("프롬프트 입력", setting.prompt(), this, false, result -> {
                    updateSetting(setting.enabled(), setting.key(), setting.model(), result,
                            setting.maxTokens(), setting.outgoingTargetLanguage(), setting.suggestionTimeout());
                }), rightX, startY + btnHeight + spacing, btnWidth, btnHeight));

        // Row 3: Target Language & Reload
        this.addDrawableChild(createButton("번역할 언어: " + setting.outgoingTargetLanguage().getName(), btn -> {
            TargetLanguage next = getNextLanguage(setting.outgoingTargetLanguage());
            updateSetting(setting.enabled(), setting.key(), setting.model(), setting.prompt(),
                    setting.maxTokens(), next, setting.suggestionTimeout());
            btn.setMessage(Text.literal("번역할 언어: " + next.getName()));
        }, leftX, startY + (btnHeight + spacing) * 2, btnWidth, btnHeight));

        this.addDrawableChild(createButton("설정 파일 다시 불러오기", btn -> {
            ClientSettingManager.loadSetting();
            MinecraftClient.getInstance().setScreen(new TranslateOptionScreen());
        }, rightX, startY + (btnHeight + spacing) * 2, btnWidth, btnHeight));

        // Row 4: Sliders (Full Width or Centered)
        this.addDrawableChild(new TokenSlider(centerX - btnWidth / 2, startY + (btnHeight + spacing) * 3 + 10,
                btnWidth, btnHeight, setting.maxTokens()));

        this.addDrawableChild(new TimeoutSlider(centerX - btnWidth / 2, startY + (btnHeight + spacing) * 4 + 10,
                btnWidth, btnHeight, setting.suggestionTimeout()));
    }

    private void updateModelButtonState() {
        Model current = models[currentModelIndex];
        boolean isWarning = current != Model.GEMINI_2_0_FLASH_LITE;

        Text message = Text.literal("모델: " + current.getModelId());
        if (isWarning) {
            message = message.copy().formatted(Formatting.RED);
            modelButton.setTooltip(Tooltip
                    .of(Text.literal("주의: 2.0 Flash Lite 외의 모델은 느리거나 비용이 높게 발생할 수 있습니다.").formatted(Formatting.RED)));
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

    private void updateSetting(boolean enabled, String key, Model model, String prompt, int maxTokens,
            TargetLanguage outgoing, long suggestionTimeout) {
        setting = new ClientSetting(enabled, key, model, prompt, maxTokens, outgoing, suggestionTimeout);
        ClientSettingManager.setSetting(setting);
    }

    private TargetLanguage getNextLanguage(TargetLanguage current) {
        return switch (current) {
            case TargetLanguage.KO -> TargetLanguage.EN;
            case TargetLanguage.EN -> TargetLanguage.JP;
            default -> TargetLanguage.KO;
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 4 - 50,
                0xFFFFFF);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }

    private ButtonWidget createButton(String text, Supplier<Screen> screenSupplier, int x, int y, int width,
            int height) {
        return createButton(text, button -> MinecraftClient.getInstance().setScreen(screenSupplier.get()), x, y,
                width, height);
    }

    private ButtonWidget createButton(String text, ButtonWidget.PressAction onPress, int x, int y, int width,
            int height) {
        return ButtonWidget.builder(Text.literal(text), onPress).dimensions(x, y, width, height).build();
    }

    private class TokenSlider extends SliderWidget {

        public TokenSlider(int x, int y, int width, int height, int initialValue) {
            super(x, y, width, height, Text.literal("최대 토큰: " + initialValue),
                    (double) (initialValue - 100) / 4900);
        }

        @Override
        protected void updateMessage() {
            int value = (int) (this.value * 4900) + 100;
            this.setMessage(Text.literal("최대 토큰: " + value));
        }

        @Override
        protected void applyValue() {
            int value = (int) (this.value * 4900) + 100;
            updateSetting(setting.enabled(), setting.key(), setting.model(), setting.prompt(), value,
                    setting.outgoingTargetLanguage(), setting.suggestionTimeout());
        }

    }

    private class TimeoutSlider extends SliderWidget {

        public TimeoutSlider(int x, int y, int width, int height, long initialValue) {
            super(x, y, width, height, Text.literal("제안 대기: " + initialValue + "ms"),
                    (double) (initialValue - 1000) / 9000);
        }

        @Override
        protected void updateMessage() {
            long value = (long) (this.value * 9000) + 1000;
            this.setMessage(Text.literal("제안 대기: " + value + "ms"));
        }

        @Override
        protected void applyValue() {
            long value = (long) (this.value * 9000) + 1000;
            updateSetting(setting.enabled(), setting.key(), setting.model(), setting.prompt(),
                    setting.maxTokens(), setting.outgoingTargetLanguage(), value);
        }

    }

}
