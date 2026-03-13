package kr.acog.translatemod.feature;

import kr.acog.translatemod.api.TranslateHandler;
import kr.acog.translatemod.config.ClientSetting;
import kr.acog.translatemod.config.ClientSettingManager;
import kr.acog.translatemod.mixin.BookScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BookTranslateFeature {

    private static BookScreen.Contents originalContents;
    private static final Map<Integer, Text> translatedPages = new IdentityHashMap<>();

    public static void initForScreen(BookScreen screen) {
        BookScreenAccessor accessor = (BookScreenAccessor) screen;
        originalContents = accessor.getContents();
        translatedPages.clear();
    }

    public static void translateCurrentPage(BookScreen screen) {
        ClientSetting setting = ClientSettingManager.getSetting();

        if (!setting.enabled()) {
            return;
        }

        BookScreenAccessor accessor = (BookScreenAccessor) screen;
        int pageIndex = accessor.getPageIndex();

        if (translatedPages.containsKey(pageIndex)) {
            return;
        }

        Text pageText = originalContents.getPage(pageIndex);
        if (pageText.getString().isBlank()) {
            return;
        }

        translatedPages.put(pageIndex, Text.translatable("translatemod.status.translating"));
        rebuildContents(screen);

        translatePreservingStructure(pageText, setting)
                .thenAccept(translated -> MinecraftClient.getInstance().execute(() -> {
                    translatedPages.put(pageIndex, translated);
                    rebuildContents(screen);
                }))
                .exceptionally(ex -> {
                    MinecraftClient.getInstance().execute(() -> {
                        translatedPages.put(pageIndex,
                                Text.translatable("translatemod.error.translation_failed", ex.getMessage()));
                        rebuildContents(screen);
                    });
                    return null;
                });
    }

    private static CompletableFuture<Text> translatePreservingStructure(Text original, ClientSetting setting) {
        IdentityHashMap<Text, CompletableFuture<String>> futures = new IdentityHashMap<>();
        collectTranslatable(original, futures, setting);

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(original);
        }

        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    IdentityHashMap<Text, String> results = new IdentityHashMap<>();
                    futures.forEach((node, future) -> results.put(node, future.join()));
                    return rebuildTree(original, results);
                });
    }

    private static void collectTranslatable(Text text, IdentityHashMap<Text, CompletableFuture<String>> futures, ClientSetting setting) {
        if (text.getContent() instanceof PlainTextContent plain && !plain.string().isBlank()) {
            futures.put(text, TranslateHandler.translateAsync(plain.string(), setting.targetLanguage(), setting));
        }
        for (Text sibling : text.getSiblings()) {
            collectTranslatable(sibling, futures, setting);
        }
    }

    private static Text rebuildTree(Text original, IdentityHashMap<Text, String> translations) {
        MutableText result;
        if (translations.containsKey(original)) {
            result = Text.literal(translations.get(original));
        } else {
            result = original.copyContentOnly();
        }
        result.setStyle(original.getStyle());
        for (Text sibling : original.getSiblings()) {
            result.append(rebuildTree(sibling, translations));
        }
        return result;
    }

    private static void rebuildContents(BookScreen screen) {
        List<Text> pages = new ArrayList<>();
        for (int i = 0; i < originalContents.getPageCount(); i++) {
            Text translated = translatedPages.get(i);
            pages.add(translated != null ? translated : originalContents.getPage(i));
        }
        screen.setPageProvider(new BookScreen.Contents(pages));
    }

}
