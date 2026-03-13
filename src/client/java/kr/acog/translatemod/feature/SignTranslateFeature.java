package kr.acog.translatemod.feature;

import kr.acog.translatemod.api.TranslateHandler;
import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.util.TranslationCache;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class SignTranslateFeature {

    private static final Map<String, String> cache = TranslationCache.create(100);
    private static volatile String lastCacheKey = null;
    private static volatile String currentTranslation = null;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(SignTranslateFeature::onTick);
        HudRenderCallback.EVENT.register(SignTranslateFeature::onHudRender);
    }

    private static void onTick(MinecraftClient client) {
        ClientSetting setting = ClientSettingManager.getSetting();

        if (!setting.enabled() || client.world == null) {
            currentTranslation = null;
            lastCacheKey = null;
            return;
        }

        BlockPos signPos = resolveSignPos(client);

        if (signPos == null) {
            currentTranslation = null;
            lastCacheKey = null;
            return;
        }

        String cacheKey = signPos.asLong() + "\0" + setting.targetLanguage().name() + "\0" + setting.translationFingerprint();

        if (cacheKey.equals(lastCacheKey)) {
            return;
        }

        lastCacheKey = cacheKey;

        String cached = cache.get(cacheKey);
        if (cached != null) {
            currentTranslation = cached;
            return;
        }

        currentTranslation = null;

        String signText = extractSignText((SignBlockEntity) client.world.getBlockEntity(signPos));
        if (signText.isBlank()) {
            return;
        }

        TranslateHandler.translateAsync(signText, setting.targetLanguage(), setting)
                .thenAccept(translated -> {
                    cache.put(cacheKey, translated);
                    if (cacheKey.equals(lastCacheKey)) {
                        currentTranslation = translated;
                    }
                })
                .exceptionally(ex -> {
                    if (cacheKey.equals(lastCacheKey)) {
                        currentTranslation = Text.translatable("translatemod.error.translation_failed", ex.getMessage()).getString();
                    }
                    return null;
                });
    }

    private static BlockPos resolveSignPos(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof BlockHitResult hit)) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        if (client.world != null && !(client.world.getBlockEntity(pos) instanceof SignBlockEntity)) {
            return null;
        }
        return pos;
    }

    private static String extractSignText(SignBlockEntity sign) {
        SignText front = sign.getFrontText();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String line = front.getMessage(i, false).getString();
            if (!line.isBlank()) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (currentTranslation == null || currentTranslation.isBlank()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int lineHeight = client.textRenderer.fontHeight + 2;

        String[] rawLines = currentTranslation.split("\n", -1);

        int maxWidth = 0;
        for (String rawLine : rawLines) {
            if (!rawLine.isBlank()) {
                maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(rawLine));
            }
        }

        int padding = 4;
        int startX = (screenWidth - maxWidth) / 2;
        int startY = screenHeight / 2 + 30;

        int lineIndex = 0;
        for (String rawLine : rawLines) {
            if (!rawLine.isBlank()) {
                int lineY = startY + lineIndex * lineHeight;
                context.fill(startX - padding, lineY - 1,
                        startX + maxWidth + padding, lineY + lineHeight - 1, 0x99000000);
                context.drawText(client.textRenderer, Text.literal(rawLine),
                        startX, lineY, 0xFFFFFFFF, false);
            }
            lineIndex++;
        }
    }

}
