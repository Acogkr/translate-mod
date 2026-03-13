package kr.acog.translatemod.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kr.acog.translatemod.access.TickingSuggesterAccessor;
import kr.acog.translatemod.api.TranslateHandler;
import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.util.TranslationCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin implements TickingSuggesterAccessor {

    @Shadow
    @Final
    TextFieldWidget textField;
    @Shadow
    private @Nullable CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    public abstract void show(boolean narrateFirstSuggestion);

    @Unique
    private boolean executed = false;
    @Unique
    private long lastTypedTime = 0;
    @Unique
    private static final Map<String, String> suggestionCache = TranslationCache.create(100);

    @Inject(method = "refresh", at = @At("HEAD"))
    private void onRefresh(CallbackInfo ci) {
        lastTypedTime = System.currentTimeMillis();
        executed = false;
    }

    @Override
    public void translateMod$tick() {
        ClientSetting setting = ClientSettingManager.getSetting();
        if (!setting.enabled()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (shouldProvideCustomSuggestions(textField.getText())
                && currentTime - lastTypedTime > setting.suggestionTimeout()
                && !executed) {
            executeCustomSuggestions();
            executed = true;
        }
    }

    @Unique
    private CompletableFuture<Suggestions> generateCustomSuggestions(String currentInput) {
        if (currentInput == null || currentInput.trim().length() < 2) {
            return CompletableFuture.completedFuture(new SuggestionsBuilder(currentInput, 0).build());
        }

        ClientSetting setting = ClientSettingManager.getSetting();
        String cacheKey = currentInput + "\0" + setting.suggestionLanguage().name() + "\0" + setting.translationFingerprint();

        String cached = suggestionCache.get(cacheKey);
        if (cached != null) {
            SuggestionsBuilder builder = new SuggestionsBuilder(currentInput, 0);
            builder.suggest(cached);
            return CompletableFuture.completedFuture(builder.build());
        }

        return TranslateHandler.translateAsync(currentInput, setting.suggestionLanguage(), setting)
                .thenApply(translated -> {
                    suggestionCache.put(cacheKey, translated);
                    SuggestionsBuilder builder = new SuggestionsBuilder(currentInput, 0);
                    builder.suggest(translated);
                    return builder.build();
                })
                .exceptionally(e -> new SuggestionsBuilder(currentInput, 0).build());
    }

    @Unique
    private boolean shouldProvideCustomSuggestions(String text) {
        return !text.startsWith("/") && !text.isEmpty();
    }

    @Unique
    private void executeCustomSuggestions() {
        CompletableFuture<Suggestions> future = generateCustomSuggestions(textField.getText());
        this.pendingSuggestions = future;

        future.thenRun(() -> {
            if (this.pendingSuggestions == future) {
                MinecraftClient.getInstance().execute(() -> this.show(true));
            }
        });
    }

}
