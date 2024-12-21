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
        String last = EnumChatFormat.getLastColors(text);
        if (!last.isEmpty()) {
            char c = last.toCharArray()[1];
            for (EnumChatFormat e : EnumChatFormat.VALUES) {
                if (e.getCharacter() == c) return TextColor.legacy(e);
            }
        }
        return null;
    }
}
