package me.neznamy.tab.shared.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is a wrapper for Adventure component created by MiniMessage. This not only
 * speeds up the performance by not having to do unnecessary conversions, but also supports
 * translatable components, which TAB components do not support.
 */
@RequiredArgsConstructor
@Getter
public class AdventureComponent extends TabComponent {

    /** The wrapped component */
    @NotNull
    private final Component component;

    @Override
    @NotNull
    public String toLegacyText() {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    @Override
    @Nullable
    protected TextColor fetchLastColor() {
        net.kyori.adventure.text.format.TextColor lastColor = component.color();
        for (Component extra : component.children()) {
            if (extra.color() != null) {
                lastColor = extra.color();
            }
        }
        return lastColor == null ? null : new TextColor(lastColor.red(), lastColor.green(), lastColor.blue());
    }
}
