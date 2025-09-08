package kr.acog.translatemod.client.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kr.acog.translatemod.api.TranslateHandler;
import kr.acog.translatemod.client.access.TickingSuggestorAccessor;
import kr.acog.translatemod.client.config.ClientSettingManager;
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

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin implements TickingSuggestorAccessor {

    @Shadow @Final TextFieldWidget textField;
    @Shadow private @Nullable CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow public abstract void show(boolean narrateFirstSuggestion);

    @Unique private boolean executed = false;
    @Unique private long lastTypedTime = 0;
    @Unique private static final long TYPED_TIMEOUT = 1000;

    @Inject(method = "refresh", at = @At("HEAD"))
    private void onRefresh(CallbackInfo ci) {
        lastTypedTime = System.currentTimeMillis();
        executed = false;
    }

    @Unique
    private CompletableFuture<Suggestions> generateCustomSuggestions(String currentInput) {
        return TranslateHandler.translateAsync(currentInput, Locale.ENGLISH, ClientSettingManager.getSetting())
                .thenApplyAsync(translated -> {
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
//        this.pendingSuggestions = generateCustomSuggestions(textField.getText());
        Suggestions suggestion = new SuggestionsBuilder("Test", 0)
                .suggest("Test Suggestion")
                .build();
        this.pendingSuggestions = CompletableFuture.completedFuture(suggestion);
        this.show(this.pendingSuggestions.isDone());
    }

    /**
     * 성능 개선을 위해 디바운스를 구현합니다.
     */
    @Override
    public void translateMod$tick() {
        long currentTime = System.currentTimeMillis();
        if (shouldProvideCustomSuggestions(textField.getText()) && currentTime - lastTypedTime > TYPED_TIMEOUT && !executed) {
            executeCustomSuggestions();
            executed = true;
        }
    }

}
