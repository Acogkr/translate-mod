package kr.acog.translatemod.gui;

import net.minecraft.client.gui.widget.TextFieldWidget.Formatter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

// TextFieldWidget의 내부 클래스 Formatter를 import 해야 합니다.
// net.minecraft.client.gui.widget.TextFieldWidget.Formatter;

public class MaskingFormatter implements Formatter {
    
    private final boolean isPassword; 

    public MaskingFormatter(boolean isPassword) {
        this.isPassword = isPassword;
    }

    @Override
    @Nullable
    public OrderedText format(String string, int firstCharacterIndex) {
        if (this.isPassword) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < string.length(); i++) {
                builder.append('*');
            }
            
            return OrderedText.styledForwardsVisitedString(builder.toString(), Style.EMPTY);
            
        }
        return null;
    }
}