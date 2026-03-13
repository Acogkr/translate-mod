package kr.acog.translatemod.mixin;

import kr.acog.translatemod.access.TickingSuggesterAccessor;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    @Shadow
    ChatInputSuggestor chatInputSuggestor;

    private ChatScreenMixin(Text title) {
        super(title);
    }

    @Override
    public void tick() {
        super.tick();
        if (chatInputSuggestor instanceof TickingSuggesterAccessor tick) {
            tick.translateMod$tick();
        }
    }

}
