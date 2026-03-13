package kr.acog.translatemod.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BookTranslateFeature {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static BookScreen.Contents originalContents;
    private static final Map<Integer, Text> translatedPages = new HashMap<>();

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
        List<TranslatableNode> nodes = new ArrayList<>();
        collectTranslatable(original, nodes);

        if (nodes.isEmpty()) {
            return CompletableFuture.completedFuture(original);
        }

        return translateTextNodes(nodes, setting)
                .thenApply(results -> rebuildTree(original, results));
    }

    private static void collectTranslatable(Text text, List<TranslatableNode> nodes) {
        if (text.getContent() instanceof PlainTextContent plain && !plain.string().isBlank()) {
            nodes.add(new TranslatableNode(text, plain.string()));
        }
        for (Text sibling : text.getSiblings()) {
            collectTranslatable(sibling, nodes);
        }
    }

    private static CompletableFuture<IdentityHashMap<Text, String>> translateTextNodes(List<TranslatableNode> nodes, ClientSetting setting) {
        String prompt = buildStructuredTranslationPrompt(nodes, setting);
        return TranslateHandler.requestPromptAsync(prompt, setting)
                .thenApply(response -> parseStructuredTranslationResponse(response, nodes));
    }

    private static String buildStructuredTranslationPrompt(List<TranslatableNode> nodes, ClientSetting setting) {
        ArrayNode input = MAPPER.createArrayNode();
        for (int i = 0; i < nodes.size(); i++) {
            ObjectNode node = input.addObject();
            node.put("id", i);
            node.put("text", nodes.get(i).originalText());
        }

        String userRule = setting.prompt() == null || setting.prompt().isBlank()
                ? "Translate naturally."
                : setting.prompt();

        return """
                Context: Minecraft book page text components
                Target Language: %s
                User Rule: %s

                You will receive a JSON array of objects. Each object has:
                - id: immutable integer
                - text: text content to translate

                Return ONLY valid JSON.
                Rules:
                - Keep the response as a JSON array.
                - Keep the same number of objects, in the same order.
                - Keep every id exactly unchanged.
                - Modify only the text values.
                - Translate every text value naturally into the target language.
                - Preserve player names, proper nouns, Minecraft terms, and emotes when appropriate.
                - Preserve line breaks inside each text value.
                - Do not add explanations, markdown, or code fences.

                Input JSON:
                %s
                """.formatted(setting.targetLanguage().getApiName(), userRule, input.toPrettyString());
    }

    private static IdentityHashMap<Text, String> parseStructuredTranslationResponse(String response, List<TranslatableNode> nodes) {
        try {
            JsonNode root = MAPPER.readTree(extractJsonArray(response));
            if (!root.isArray() || root.size() != nodes.size()) {
                throw new IllegalArgumentException("응답 JSON 배열 크기가 올바르지 않습니다.");
            }

            IdentityHashMap<Text, String> results = new IdentityHashMap<>();
            for (int i = 0; i < nodes.size(); i++) {
                JsonNode entry = root.get(i);
                if (entry == null || !entry.isObject()) {
                    throw new IllegalArgumentException("응답 항목 형식이 올바르지 않습니다.");
                }

                int id = entry.path("id").asInt(-1);
                if (id != i) {
                    throw new IllegalArgumentException("응답 id 순서가 올바르지 않습니다.");
                }

                JsonNode translatedNode = entry.get("text");
                if (translatedNode == null || translatedNode.isNull()) {
                    throw new IllegalArgumentException("응답 text 값이 비어 있습니다.");
                }

                results.put(nodes.get(i).node(), translatedNode.asText());
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("책 번역 응답 파싱 실패", e);
        }
    }

    private static String extractJsonArray(String response) {
        String trimmed = response.trim();
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            trimmed = firstNewline >= 0 ? trimmed.substring(firstNewline + 1, trimmed.length() - 3).trim() : trimmed;
        }

        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("JSON 배열을 찾을 수 없습니다.");
        }
        return trimmed.substring(start, end + 1);
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

    private record TranslatableNode(Text node, String originalText) {
    }

}
