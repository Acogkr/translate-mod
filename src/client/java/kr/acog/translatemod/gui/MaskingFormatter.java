package kr.acog.translatemod.gui;

import net.minecraft.client.gui.widget.TextFieldWidget.Formatter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

public class MaskingFormatter implements Formatter {
    
    private final boolean isPassword; 

    public MaskingFormatter(boolean isPassword) {
        this.isPassword = isPassword;
    }

    @Override
    @Nullable
    public OrderedText format(String string, int firstCharacterIndex) {
        if (this.isPassword) {
            return OrderedText.styledForwardsVisitedString("*".repeat(string.length()), Style.EMPTY);
        }
        return null;
    }
}