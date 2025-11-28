package kr.acog.translatemod.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kr.acog.translatemod.api.TranslateHandler;

import kr.acog.translatemod.access.TickingSuggesterAccessor;
import kr.acog.translatemod.config.ClientSettingManager;
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
    private static final int MAX_CACHE_SIZE = 100;
    private static final Map<String, String> suggestionCache = java.util.Collections.synchronizedMap(
            new java.util.LinkedHashMap<String, String>(MAX_CACHE_SIZE + 1, .75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });

    @Inject(method = "refresh", at = @At("HEAD"))
    private void onRefresh(CallbackInfo ci) {
        lastTypedTime = System.currentTimeMillis();
        executed = false;
    }

    @Override
    public void translateMod$tick() {
        if (!ClientSettingManager.getSetting().enabled()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long timeout = ClientSettingManager.getSetting().suggestionTimeout();
        if (shouldProvideCustomSuggestions(textField.getText()) && currentTime - lastTypedTime > timeout
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

        String cached = suggestionCache.get(currentInput);
        if (cached != null) {
            SuggestionsBuilder builder = new SuggestionsBuilder(currentInput, 0);
            builder.suggest(cached);
            return CompletableFuture.completedFuture(builder.build());
        }

        if (pendingSuggestions != null && !pendingSuggestions.isDone()) {
            return pendingSuggestions;
        }

        return TranslateHandler
                .translateAsync(currentInput, ClientSettingManager.getSetting().outgoingTargetLanguage(),
                        ClientSettingManager.getSetting())
                .thenApplyAsync(translated -> {
                    suggestionCache.put(currentInput, translated);
                    SuggestionsBuilder builder = new SuggestionsBuilder(currentInput, 0);
                    builder.suggest(translated);
                    return builder.build();
                }, MinecraftClient.getInstance())
                .exceptionally(e -> {
                    SuggestionsBuilder errorBuilder = new SuggestionsBuilder(currentInput, 0);
                    errorBuilder.suggest("번역 실패");
                    return errorBuilder.build();
                });
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
                this.show(true);
            }
        });
    }

}
