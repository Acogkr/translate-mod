package kr.acog.translatemod;

import kr.acog.translatemod.access.TickingSuggesterAccessor;
import kr.acog.translatemod.api.TranslateHandler;
import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.gui.TranslateOptionScreen;
import kr.acog.translatemod.mixin.ChatScreenAccessor;
import kr.acog.translatemod.type.TargetLanguage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

public class TranslateModClient implements ClientModInitializer {

    public static KeyBinding toggleKeyBinding;

    @Override
    public void onInitializeClient() {
        ClientSettingManager.loadSetting();

        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("translatemod.option.key_bind",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, KeyBinding.Category.MULTIPLAYER));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKeyBinding.wasPressed()) {
                ClientSetting setting = ClientSettingManager.getSetting();
                boolean newState = !setting.enabled();

                if (client.player != null) {
                    client.player.sendMessage(Text.translatable(newState ? "translatemod.option.enabled" : "translatemod.option.disabled"), false);
                }
                ClientSettingManager.setSetting(new ClientSetting(newState, setting.key(), setting.mode(), setting.model(), setting.prompt(), setting.maxTokens(), setting.targetLanguage(), 3000L));
            }

            if (client.currentScreen instanceof ChatScreen chatScreen) {
                ChatScreenAccessor accessor = (ChatScreenAccessor) chatScreen;
                ChatInputSuggestor suggestor = accessor.getChatInputSuggestor();
                if (suggestor instanceof TickingSuggesterAccessor tick) {
                    tick.translateMod$tick();
                }
            }
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!ClientSettingManager.getSetting().enabled() || overlay) {
                return true;
            }
            String content = message.getString();
            if (shouldTranslate(content)) {
                sendTranslateMessage(message, content);
                return false;
            }
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!ClientSettingManager.getSetting().enabled()) {
                return true;
            }

            String rawContent = message.getString();
            assert sender != null;
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
                sendTranslateMessage(displayComponent, contentToTranslate);
                return false;
            }
            return true;
        });
    }

    private String removePlayerName(String fullText, String playerName) {
        String pattern = "<" + Pattern.quote(playerName) + ">\\s*";
        String cleaned = fullText.replaceFirst("^" + pattern, "");

        return cleaned.isEmpty() ? fullText : cleaned;
    }

    private boolean shouldTranslate(String raw) {
        if (raw == null || raw.isEmpty() || raw.startsWith("/")) {
            return false;
        }
        return true;
    }

    private void sendTranslateMessage(Text displayComponent, String contentToTranslate) {
        MutableText withHover = displayComponent.copy();
        TargetLanguage targetLocale = getClientLanguage();

        TranslateHandler.translateAsync(contentToTranslate, targetLocale, ClientSettingManager.getSetting())
                .thenAccept(translated -> {
                    MinecraftClient.getInstance().execute(() -> {
                        HoverEvent newHoverEvent = new HoverEvent.ShowText(Text.empty().append(targetLocale.getPrefix()).append(translated));
                        withHover.setStyle(withHover.getStyle().withHoverEvent(newHoverEvent));
                        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(withHover);
                    });
                }).exceptionally(ex -> {
                    MinecraftClient.getInstance().execute(() -> {
                        HoverEvent errorHover = new HoverEvent.ShowText(Text.translatable("translatemod.error.translation_failed", ex.getMessage()));
                        withHover.setStyle(withHover.getStyle().withHoverEvent(errorHover));
                        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(withHover);
                    });
                    return null;
                });
    }

    private TargetLanguage getClientLanguage() {
        String code = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        if (code.startsWith("ko")) return TargetLanguage.KO;
        if (code.startsWith("en")) return TargetLanguage.EN;
        if (code.startsWith("ja")) return TargetLanguage.JA;
        return ClientSettingManager.getSetting().targetLanguage();
    }
}