package kr.acog.translatemod.client.screen;

import kr.acog.translatemod.api.ProviderType;
import kr.acog.translatemod.client.config.ClientSetting;
import kr.acog.translatemod.client.config.ClientSettingManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class TranslateOptionScreen extends Screen {

    private int currentIndex = 0;
    private ClientSetting setting;
    private final ProviderType[] providers = ProviderType.values();

    public TranslateOptionScreen() {
        super(
                Text.literal("Translate Mod Options")
        );
    }

    @Override
    protected void init() {
        super.init();

        setting = ClientSettingManager.getSetting();
        currentIndex = IntStream.range(0, providers.length)
                .filter(i -> providers[i] == setting.type())
                .findFirst()
                .orElse(0);

        int btnWidth = 120;
        int btnHeight = 20;
        int spacing = 10;
        int totalWidth = btnWidth * 3 + spacing * 2;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height / 2;

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal(providers[currentIndex].getName()),
                        btn -> {
                            currentIndex = (currentIndex + 1) % providers.length;
                            setting = new ClientSetting(providers[currentIndex], setting.key(), setting.prompt());

                            ClientSettingManager.setSetting(setting);
                            btn.setMessage(Text.literal(providers[currentIndex].getName()));
                        }
                )
                .dimensions(startX, y, btnWidth, btnHeight)
                .build()
        );

        this.addDrawableChild(
                createButton(
                        "Set API Key",
                        () -> new TextInputScreen(String.format("Input %s API Key", providers[currentIndex].getName()), setting.key(), this, true, result -> {
                            setting = new ClientSetting(setting.type(), result, setting.prompt());
                            ClientSettingManager.setSetting(setting);
                        }),
                        startX + btnWidth + spacing, y, btnWidth, btnHeight
                )
        );

        this.addDrawableChild(
                createButton(
                        "Set Prompt",
                        () -> new TextInputScreen("Input Prompt", setting.prompt(), this, false, result -> {
                            setting = new ClientSetting(setting.type(), setting.key(), result);
                            ClientSettingManager.setSetting(setting);
                        }),
                        startX + 2 * (btnWidth + spacing), y, btnWidth, btnHeight
                )
        );

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                this.height / 2 - 40,
                0xFFFFFF
        );

    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }

    private ButtonWidget createButton(String text, Supplier<Screen> screenSupplier, int x, int y, int width, int height) {
        return ButtonWidget.builder(
                Text.literal(text),
                button -> MinecraftClient.getInstance().setScreen(screenSupplier.get())
        ).dimensions(x, y, width, height).build();
    }

}
