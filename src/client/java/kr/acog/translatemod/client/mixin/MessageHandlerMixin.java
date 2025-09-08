package kr.acog.translatemod.client.mixin;

import kr.acog.translatemod.api.TranslateHandler;
import kr.acog.translatemod.client.config.ClientSettingManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {

    @Unique
    private static final String prefix = "[번역] ";

    @Inject(
            method = "onGameMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        if (shouldTranslate(message)) {
            ci.cancel();
            sendTranslateMessage(message);
        }
    }

    @Unique
    private boolean shouldTranslate(Text original) {
        String raw = original.getString();

        if (raw.isEmpty() || raw.startsWith("/")) {
            return false;
        }

        HoverEvent hoverEvent = original.getStyle().getHoverEvent();
        if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
            Text value = hoverEvent.getContents(HoverEvent.Action.SHOW_TEXT);
            if (value != null && value.getString().contains(prefix)) {
                return false;
            }
        }

        return true;
    }

    @Unique
    private void sendTranslateMessage(Text original) {
        MutableText withHover = original.copy();

        TranslateHandler.translateAsync(
                original.getString(),
                Locale.KOREAN,
                ClientSettingManager.getSetting()
        )
        .thenAccept(translated -> {
            MinecraftClient.getInstance().execute(() -> {
                HoverEvent newHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(prefix + translated));
                withHover.setStyle(withHover.getStyle().withHoverEvent(newHoverEvent));

                MinecraftClient.getInstance().inGameHud
                        .getChatHud()
                        .addMessage(withHover);
            });
        })
        .exceptionally(ex -> {
            MinecraftClient.getInstance().inGameHud
                    .getChatHud()
                    .addMessage(original);
            return null;
        });
    }
}

