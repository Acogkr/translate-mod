package kr.acog.translatemod.mixin;

import net.minecraft.client.gui.screen.ingame.BookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BookScreen.class)
public interface BookScreenAccessor {

    @Accessor("contents")
    BookScreen.Contents getContents();

    @Accessor("pageIndex")
    int getPageIndex();

}
