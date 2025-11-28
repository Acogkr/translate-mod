package kr.acog.translatemod.mixin;

import kr.acog.translatemod.api.TranslateHandler;
import kr.acog.translatemod.type.TargetLanguage;
import kr.acog.translatemod.config.ClientSettingManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import com.mojang.authlib.GameProfile;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        if (!ClientSettingManager.getSetting().enabled()) {
            return;
        }

        if (overlay) {
            return;
        }

        if (shouldTranslate(message)) {
            ci.cancel();
            sendTranslateMessage(message);
        }
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params,
                               CallbackInfo ci) {
        if (!ClientSettingManager.getSetting().enabled()) {
            return;
        }

        Text formatted = params.applyChatDecoration(message.getContent());
        if (shouldTranslate(formatted)) {
            ci.cancel();
            sendTranslateMessage(formatted);
        }
    }

    @Unique
    private boolean shouldTranslate(Text original) {
        String raw = original.getString();

        if (raw.isEmpty() || raw.startsWith("/")) {
            return false;
        }

//        boolean hasHover = original.visit((style, asString) -> {
//            if (style != null && style.getHoverEvent() != null) {
//                return Optional.of(true);
//            }
//            return Optional.empty();
//        }, Style.EMPTY).orElse(false);

        return true;
    }

    @Unique
    private void sendTranslateMessage(Text original) {
        MutableText withHover = original.copy();
        TargetLanguage targetLocale = TargetLanguage.KO;

        TranslateHandler.translateAsync(original.getString(), targetLocale, ClientSettingManager.getSetting())
                .thenAccept(translated -> {
                    MinecraftClient.getInstance().execute(() -> {
                        HoverEvent newHoverEvent = new HoverEvent.ShowText(Text.literal(targetLocale.getPrefix() + translated));
                        withHover.setStyle(withHover.getStyle().withHoverEvent(newHoverEvent));
                        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(withHover);
                    });
                })
                .exceptionally(ex -> {
                    MinecraftClient.getInstance().execute(() -> {
                        HoverEvent errorHover = new HoverEvent.ShowText(Text.literal("번역 실패: " + ex.getMessage()));
                        withHover.setStyle(withHover.getStyle().withHoverEvent(errorHover));
                        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(withHover);
                    });
                    return null;
                });
    }
}