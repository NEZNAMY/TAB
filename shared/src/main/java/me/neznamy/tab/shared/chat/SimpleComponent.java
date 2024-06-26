package me.neznamy.tab.shared.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    public String toRawText() {
        return text;
    }

}
