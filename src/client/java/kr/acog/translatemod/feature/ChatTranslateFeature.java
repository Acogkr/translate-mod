package kr.acog.translatemod.feature;

import kr.acog.translatemod.api.TranslateHandler;
import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.type.TargetLanguage;
import kr.acog.translatemod.type.TranslateScope;
import kr.acog.translatemod.util.TranslationCache;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTranslateFeature {

    private static final Map<String, String> cache = TranslationCache.create(100);

    private static final Pattern PLAYER_CHAT_PATTERN = Pattern.compile("^(?:\\[[^]]*]\\s+)*<[^>]+>\\s+(.+)", Pattern.DOTALL);

    public static void register() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            ClientSetting setting = ClientSettingManager.getSetting();
            if (!setting.enabled() || overlay) {
                return true;
            }
            String content = message.getString();
            if (!shouldTranslate(content)) {
                return true;
            }
            if (setting.scope() == TranslateScope.CHAT_ONLY) {
                Matcher matcher = PLAYER_CHAT_PATTERN.matcher(content);
                if (!matcher.matches()) {
                    return true;
                }
                sendTranslateMessage(message, matcher.group(1), setting);
                return false;
            }
            sendTranslateMessage(message, content, setting);
            return false;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            ClientSetting setting = ClientSettingManager.getSetting();
            if (!setting.enabled()) {
                return true;
            }
            if (sender == null) {
                return true;
            }
            String rawContent = message.getString();
            String playerName = sender.name();

            Text displayComponent;
            String contentToTranslate;

            if (rawContent.contains(playerName)) {
                displayComponent = message;
                contentToTranslate = removePlayerName(rawContent, playerName);
            } else {
                displayComponent = params.applyChatDecoration(message);
                contentToTranslate = rawContent;
            }

            if (shouldTranslate(contentToTranslate)) {
                sendTranslateMessage(displayComponent, contentToTranslate, setting);
                return false;
            }
            return true;
        });
    }

    private static boolean shouldTranslate(String raw) {
        return raw != null && !raw.isEmpty() && !raw.startsWith("/");
    }

    private static String removePlayerName(String fullText, String playerName) {
        String cleaned = fullText.replaceFirst("^<" + Pattern.quote(playerName) + ">\\s*", "");
        return cleaned.isEmpty() ? fullText : cleaned;
    }

    private static void sendTranslateMessage(Text displayComponent, String content, ClientSetting setting) {
        MutableText withHover = displayComponent.copy();
        String cacheKey = content + "\0" + setting.targetLanguage().name() + "\0" + setting.translationFingerprint();

        String cached = cache.get(cacheKey);
        if (cached != null) {
            applyTranslation(withHover, setting.targetLanguage(), cached);
            return;
        }

        TranslateHandler.translateAsync(content, setting.targetLanguage(), setting)
                .thenAccept(translated -> {
                    cache.put(cacheKey, translated);
                    MinecraftClient.getInstance().execute(() ->
                            applyTranslation(withHover, setting.targetLanguage(), translated));
                })
                .exceptionally(ex -> {
                    MinecraftClient.getInstance().execute(() -> {
                        HoverEvent errorHover = new HoverEvent.ShowText(
                                Text.translatable("translatemod.error.translation_failed", ex.getMessage()));
                        withHover.setStyle(withHover.getStyle().withHoverEvent(errorHover));
                        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(withHover);
                    });
                    return null;
                });
    }

    private static void applyTranslation(MutableText message, TargetLanguage targetLanguage, String translated) {
        HoverEvent hoverEvent = new HoverEvent.ShowText(Text.empty().append(targetLanguage.getPrefix()).append(translated));
        message.setStyle(message.getStyle().withHoverEvent(hoverEvent));
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
    }

}
