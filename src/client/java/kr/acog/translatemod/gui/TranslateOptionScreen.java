package kr.acog.translatemod.gui;

import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.type.Model;
import kr.acog.translatemod.type.PromptMode;
import kr.acog.translatemod.type.Provider;
import kr.acog.translatemod.type.TargetLanguage;
import kr.acog.translatemod.type.TranslateScope;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TranslateOptionScreen extends Screen {

    private ClientSetting setting;

    public TranslateOptionScreen() {
        super(Text.translatable("translatemod.title.options"));
    }

    @Override
    protected void init() {
        super.init();
        setting = ClientSettingManager.getSetting();

        int buttonWidth = 150;
        int buttonHeight = 20;
        int gap = 5;
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int leftX = centerX - buttonWidth - 5;
        int rightX = centerX + 5;

        addDrawableChild(createButton(
                Text.translatable(setting.enabled() ? "translatemod.option.enabled" : "translatemod.option.disabled"),
                btn -> {
                    boolean next = !setting.enabled();
                    updateSetting(setting.withEnabled(next));
                    btn.setMessage(Text.translatable(next ? "translatemod.option.enabled" : "translatemod.option.disabled"));
                }, leftX, startY, buttonWidth, buttonHeight));

        addDrawableChild(createButton(
                Text.translatable("translatemod.option.provider", setting.model().getProvider().getLabel().getString()),
                btn -> cycleProvider(), rightX, startY, buttonWidth, buttonHeight));

        Provider provider = setting.model().getProvider();
        if (provider == Provider.OLLAMA) {
            addOllamaRow(leftX, rightX, startY + (buttonHeight + gap), buttonWidth, buttonHeight);
        } else {
            addModelAndKeyRow(leftX, rightX, startY + (buttonHeight + gap), buttonWidth, buttonHeight);
        }

        addDrawableChild(createButton(
                Text.translatable("translatemod.option.my_language", setting.targetLanguage().getName().getString()),
                btn -> openLanguageList(
                        Text.translatable("translatemod.screen.my_language.title"),
                        lang -> updateSetting(setting.withTargetLanguage(lang))),
                leftX, startY + (buttonHeight + gap) * 2, buttonWidth, buttonHeight));

        addDrawableChild(createButton(
                Text.translatable("translatemod.option.suggestion_language", setting.suggestionLanguage().getName().getString()),
                btn -> openLanguageList(
                        Text.translatable("translatemod.screen.suggestion_language.title"),
                        lang -> updateSetting(setting.withSuggestionLanguage(lang))),
                rightX, startY + (buttonHeight + gap) * 2, buttonWidth, buttonHeight));

        addDrawableChild(createButton(
                Text.translatable("translatemod.option.prompt_mode", setting.mode().getLabel().getString()),
                btn -> {
                    PromptMode next = nextEnum(setting.mode());
                    updateSetting(setting.withMode(next));
                    btn.setMessage(Text.translatable("translatemod.option.prompt_mode", next.getLabel().getString()));
                }, leftX, startY + (buttonHeight + gap) * 3, buttonWidth, buttonHeight));

        addDrawableChild(createButton(
                Text.translatable("translatemod.option.scope", setting.scope().getLabel().getString()),
                btn -> {
                    TranslateScope next = nextEnum(setting.scope());
                    updateSetting(setting.withScope(next));
                    btn.setMessage(Text.translatable("translatemod.option.scope", next.getLabel().getString()));
                }, rightX, startY + (buttonHeight + gap) * 3, buttonWidth, buttonHeight));

        addDrawableChild(createButton(
                Text.translatable("translatemod.option.prompt_settings"),
                () -> new TextInputScreen(Text.translatable("translatemod.screen.prompt.title"),
                        setting.prompt(), this, false,
                        result -> updateSetting(setting.withPrompt(result))),
                centerX - buttonWidth / 2, startY + (buttonHeight + gap) * 4, buttonWidth, buttonHeight));

        addDrawableChild(new SettingSlider(
                centerX - buttonWidth / 2, startY + (buttonHeight + gap) * 5 + 5, buttonWidth, buttonHeight,
                100, 4900, "translatemod.option.max_tokens", setting.maxTokens(),
                value -> updateSettingInMemory(setting.withMaxTokens((int) (long) value))));
        addDrawableChild(new SettingSlider(
                centerX - buttonWidth / 2, startY + (buttonHeight + gap) * 6 + 5, buttonWidth, buttonHeight,
                1000, 9000, "translatemod.option.suggestion_delay", setting.suggestionTimeout(),
                value -> updateSettingInMemory(setting.withSuggestionTimeout(value))));
    }

    private void addModelAndKeyRow(int leftX, int rightX, int startY, int buttonWidth, int buttonHeight) {
        addDrawableChild(createButton(
                Text.literal(setting.model().getModelId()),
                btn -> openModelList(), leftX, startY, buttonWidth, buttonHeight));

        boolean hasKey = !setting.currentApiKey().isEmpty();
        Text keyLabel = hasKey
                ? Text.translatable("translatemod.option.api_key_set").formatted(Formatting.GREEN)
                : Text.translatable("translatemod.option.api_key").formatted(Formatting.RED);
        addDrawableChild(createButton(keyLabel,
                () -> new TextInputScreen(Text.translatable("translatemod.screen.api_key.title"),
                        setting.currentApiKey(), this, true,
                        result -> {
                            Map<Provider, String> keys = new HashMap<>(setting.apiKeys() == null ? new HashMap<>() : setting.apiKeys());
                            keys.put(setting.model().getProvider(), result);
                            updateSetting(setting.withApiKeys(keys));
                        }),
                rightX, startY, buttonWidth, buttonHeight));
    }

    private void addOllamaRow(int leftX, int rightX, int startY, int buttonWidth, int buttonHeight) {
        addDrawableChild(createButton(
                Text.translatable("translatemod.option.ollama_host", setting.baseUrl()),
                () -> new TextInputScreen(Text.translatable("translatemod.screen.ollama_host.title"),
                        setting.baseUrl(), this, false,
                        result -> updateSetting(setting.withBaseUrl(result))),
                leftX, startY, buttonWidth, buttonHeight));

        addDrawableChild(createButton(
                Text.translatable("translatemod.option.ollama_model", setting.customModelId()),
                () -> new TextInputScreen(Text.translatable("translatemod.screen.ollama_model.title"),
                        setting.customModelId(), this, false,
                        result -> updateSetting(setting.withCustomModelId(result))),
                rightX, startY, buttonWidth, buttonHeight));
    }

    private void openModelList() {
        List<Model> models = Model.forProvider(setting.model().getProvider());
        MinecraftClient.getInstance().setScreen(new ListSelectScreen<>(
                Text.translatable("translatemod.screen.model.title"),
                this,
                models,
                model -> Text.literal(model.getModelId()),
                model -> {
                    updateSetting(setting.withModel(model));
                    MinecraftClient.getInstance().setScreen(new TranslateOptionScreen());
                }
        ));
    }

    private void openLanguageList(Text title, Consumer<TargetLanguage> onSelect) {
        MinecraftClient.getInstance().setScreen(new ListSelectScreen<>(
                title,
                this,
                Arrays.asList(TargetLanguage.values()),
                TargetLanguage::getName,
                lang -> {
                    onSelect.accept(lang);
                    MinecraftClient.getInstance().setScreen(new TranslateOptionScreen());
                }
        ));
    }

    private void cycleProvider() {
        Provider[] providers = Provider.values();
        Provider next = providers[(setting.model().getProvider().ordinal() + 1) % providers.length];
        Model defaultModel = Model.forProvider(next).get(0);
        updateSetting(setting.withModel(defaultModel));
        MinecraftClient.getInstance().setScreen(new TranslateOptionScreen());
    }

    private void updateSetting(ClientSetting newSetting) {
        setting = newSetting;
        ClientSettingManager.setSetting(setting);
    }

    private void updateSettingInMemory(ClientSetting newSetting) {
        setting = newSetting;
        ClientSettingManager.setSettingWithoutSave(newSetting);
    }

    private static <T extends Enum<T>> T nextEnum(T current) {
        T[] values = current.getDeclaringClass().getEnumConstants();
        return values[(current.ordinal() + 1) % values.length];
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 4 - 50, 0xFFFFFFFF);

        if (setting.model().isExpensive()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("translatemod.warning.model_cost"),
                    width / 2, height / 4 - 32, 0xFFFFFF55);
        }
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }

    private ButtonWidget createButton(Text text, Supplier<Screen> screenSupplier, int x, int y, int width, int height) {
        return createButton(text, btn -> MinecraftClient.getInstance().setScreen(screenSupplier.get()), x, y, width, height);
    }

    private ButtonWidget createButton(Text text, ButtonWidget.PressAction onPress, int x, int y, int width, int height) {
        return ButtonWidget.builder(text, onPress).dimensions(x, y, width, height).build();
    }

    private class SettingSlider extends SliderWidget {

        private final long minValue;
        private final long range;
        private final String translationKey;
        private final Consumer<Long> applier;

        public SettingSlider(int x, int y, int width, int height,
                             long minValue, long range, String translationKey, long initialValue,
                             Consumer<Long> applier) {
            super(x, y, width, height,
                    Text.translatable(translationKey, initialValue),
                    (double) (initialValue - minValue) / range);
            this.minValue = minValue;
            this.range = range;
            this.translationKey = translationKey;
            this.applier = applier;
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.translatable(translationKey, computeValue()));
        }

        @Override
        protected void applyValue() {
            applier.accept(computeValue());
        }

        @Override
        public void onRelease(Click click) {
            super.onRelease(click);
            ClientSettingManager.saveSetting();
        }

        private long computeValue() {
            return (long) (this.value * range) + minValue;
        }

    }

}
