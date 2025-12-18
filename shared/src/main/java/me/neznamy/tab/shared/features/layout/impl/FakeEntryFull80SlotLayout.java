package me.neznamy.tab.shared.features.layout.impl;

import lombok.Getter;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.layout.impl.common.LayoutPattern;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Layout implementation for 80-slot tablist that simply pushes all real entries
 * out of the tablist. Available on all versions starting from 1.8.
 */
@Getter
public class FakeEntryFull80SlotLayout extends LayoutBase {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   manager
     *          Layout manager
     * @param   pattern
     *          Layout pattern
     * @param   viewer
     *          Viewer of the layout
     */
    public FakeEntryFull80SlotLayout(@NotNull LayoutManagerImpl manager, @NotNull LayoutPattern pattern, @NotNull TabPlayer viewer) {
        super(manager, pattern, viewer, 80);
    }
}
