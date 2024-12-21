package me.neznamy.tab.shared.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple component with only text using legacy colors and nothing else.
 */
@Getter
@RequiredArgsConstructor
public class SimpleComponent extends TabComponent {

    @NotNull
    private final String text;

    @Override
    @NotNull
    public String toLegacyText() {
        return text;
    }

    @Override
    @Nullable
    protected TextColor fetchLastColor() {
        char[] chars = text.toCharArray();
        for (int index = chars.length - 2; index >= 0; index--) {
            if (chars[index] != '§') continue;
            EnumChatFormat color = EnumChatFormat.getByChar(chars[index + 1]);
            if (color != null) return TextColor.legacy(color);
        }
        return null;
    }
}
