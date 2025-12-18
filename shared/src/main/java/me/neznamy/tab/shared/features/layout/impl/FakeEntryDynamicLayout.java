package me.neznamy.tab.shared.features.layout.impl;

import lombok.Getter;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.layout.impl.common.LayoutPattern;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Layout implementation for dynamic slot count that creates fake entries to display
 * the layout and hides real players using 1.19.3+ listed option.
 */
@Getter
public class FakeEntryDynamicLayout extends LayoutBase {

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
    public FakeEntryDynamicLayout(@NotNull LayoutManagerImpl manager, @NotNull LayoutPattern pattern, @NotNull TabPlayer viewer) {
        super(manager, pattern, viewer, pattern.getSlotCount());
    }

    @Override
    public void send() {
        super.send();
        viewer.getTabList().hideAllPlayers();
    }

    @Override
    public void destroy() {
        super.destroy();
        viewer.getTabList().showAllPlayers();
    }
}
