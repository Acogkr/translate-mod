package kr.acog.translatemod.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ListSelectScreen<T> extends Screen {

    private static final int ENTRY_HEIGHT = 20;
    private static final int SEARCH_TOP = 30;
    private static final int LIST_TOP = 60;
    private static final int FOOTER_HEIGHT = 35;

    private final Screen parent;
    private final List<T> allItems;
    private final Function<T, Text> labelOf;
    private final Consumer<T> onSelect;

    private List<T> filtered = new ArrayList<>();
    private int scrollOffset = 0;

    public ListSelectScreen(Text title, Screen parent, List<T> allItems, Function<T, Text> labelOf, Consumer<T> onSelect) {
        super(title);
        this.parent = parent;
        this.allItems = allItems;
        this.labelOf = labelOf;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        updateFilter("");

        TextFieldWidget searchField = new TextFieldWidget(textRenderer, width / 2 - 100, SEARCH_TOP, 200, 20, Text.literal(""));
        searchField.setMaxLength(64);
        searchField.setSuggestion(Text.translatable("translatemod.list.search").getString());
        searchField.setChangedListener(query -> {
            updateFilter(query);
            scrollOffset = 0;
        });
        addDrawableChild(searchField);

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("translatemod.button.cancel"),
                btn -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(width / 2 - 50, height - FOOTER_HEIGHT + 5, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFFFF);

        int listHeight = height - LIST_TOP - FOOTER_HEIGHT;
        int firstIndex = scrollOffset / ENTRY_HEIGHT;

        for (int i = firstIndex; i < filtered.size(); i++) {
            int itemY = LIST_TOP + i * ENTRY_HEIGHT - scrollOffset;
            if (itemY < LIST_TOP) {
                continue;
            }
            if (itemY + ENTRY_HEIGHT > LIST_TOP + listHeight) {
                break;
            }

            boolean hovered = mouseY >= itemY && mouseY < itemY + ENTRY_HEIGHT;
            if (hovered) {
                context.fill(0, itemY, width, itemY + ENTRY_HEIGHT, 0x33FFFFFF);
            }
            int textColor = hovered ? 0xFFFFFF55 : 0xFFFFFFFF;

            context.drawCenteredTextWithShadow(textRenderer,
                    labelOf.apply(filtered.get(i)), width / 2, itemY + 5, textColor);
        }
    }

    private void updateFilter(String query) {
        if (query == null || query.isBlank()) {
            filtered = new ArrayList<>(allItems);
        } else {
            String lower = query.toLowerCase();
            filtered = allItems.stream()
                    .filter(item -> labelOf.apply(item).getString().toLowerCase().contains(lower))
                    .toList();
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int listHeight = height - LIST_TOP - FOOTER_HEIGHT;
        if (click.y() >= LIST_TOP && click.y() < LIST_TOP + listHeight) {
            int index = ((int) click.y() - LIST_TOP + scrollOffset) / ENTRY_HEIGHT;
            if (index >= 0 && index < filtered.size()) {
                onSelect.accept(filtered.get(index));
                return true;
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int listHeight = height - LIST_TOP - FOOTER_HEIGHT;
        int maxScroll = Math.max(0, filtered.size() * ENTRY_HEIGHT - listHeight);
        scrollOffset = Math.clamp((int) (scrollOffset - verticalAmount * ENTRY_HEIGHT), 0, maxScroll);
        return true;
    }

}
