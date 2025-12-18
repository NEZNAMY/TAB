package me.neznamy.tab.shared.features.layout.impl;

import lombok.Getter;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.features.layout.LayoutConfiguration;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.layout.impl.common.PlayerGroup;
import me.neznamy.tab.shared.features.layout.impl.common.PlayerSlot;
import me.neznamy.tab.shared.features.layout.impl.common.FixedSlot;
import me.neznamy.tab.shared.features.layout.impl.common.LayoutPattern;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Base class for all layout implementations.
 */
@Getter
public class LayoutBase {

    @NotNull
    private final LayoutManagerImpl manager;

    @NotNull
    private final LayoutPattern pattern;

    @NotNull
    protected final TabPlayer viewer;

    @NotNull
    private final List<Integer> emptySlots;

    @NotNull
    private final UUID[] allUUIDs;

    @NotNull
    private final Collection<FixedSlot> fixedSlots;

    @NotNull
    private final List<PlayerGroup> groups = new ArrayList<>();

    /**
     * Constructs new instance with given parameters.
     *
     * @param   manager
     *          Layout manager
     * @param   pattern
     *          Layout pattern
     * @param   viewer
     *          Viewer of the layout
     * @param   slotCount
     *          Total amount of slots to show
     */
    public LayoutBase(@NotNull LayoutManagerImpl manager, @NotNull LayoutPattern pattern, @NotNull TabPlayer viewer,
                      int slotCount) {
        this.manager = manager;
        this.viewer = viewer;
        this.pattern = pattern;
        emptySlots = IntStream.range(1, slotCount + 1).boxed().collect(Collectors.toList());
        allUUIDs = Arrays.copyOf(manager.getUuids(), slotCount);
        fixedSlots = pattern.getFixedSlots().values();
        for (FixedSlot slot : fixedSlots) {
            emptySlots.remove((Integer) slot.getSlot());
        }
        for (LayoutConfiguration.LayoutDefinition.GroupPattern group : pattern.getGroups()) {
            emptySlots.removeAll(Arrays.stream(group.getSlots()).boxed().collect(Collectors.toList()));
            groups.add(new PlayerGroup(this, group));
        }
    }

    /**
     * Sends the layout to viewer.
     */
    public void send() {
        for (PlayerGroup group : groups) {
            group.sendAll();
        }
        for (FixedSlot slot : fixedSlots) {
            viewer.getTabList().addEntry(slot.createEntry(viewer));
        }
        for (int slot : emptySlots) {
            viewer.getTabList().addEntry(new TabList.Entry(
                    manager.getUUID(slot),
                    manager.getConfiguration().getDirection().getEntryName(viewer, slot, LayoutManagerImpl.isTeamsEnabled()),
                    pattern.getDefaultSkin(slot),
                    true,
                    manager.getConfiguration().getEmptySlotPing(),
                    0,
                    TabComponent.empty(),
                    Integer.MAX_VALUE - manager.getConfiguration().getDirection().translateSlot(slot),
                    true
            ));
        }
        tick();
    }

    /**
     * Removes all entries added by this layout from viewer's tablist.
     */
    public void destroy() {
        for (UUID id : allUUIDs) {
            viewer.getTabList().removeEntry(id);
        }
    }

    /**
     * Ticks all players. This may end up moving players into different groups.
     */
    public void tick() {
        List<TabPlayer> players = manager.getSortedPlayers().keySet().stream().filter(viewer::canSee).collect(Collectors.toList());
        for (PlayerGroup group : groups) {
            group.tick(players);
        }
    }

    /**
     * Returns slot in which specified player is present. If player is not found, {@code null} is returned.
     *
     * @param   target
     *          Player to search for
     * @return  Slot of specified player or {@code null} if not found
     */
    @Nullable
    public PlayerSlot getSlot(@NotNull TabPlayer target) {
        for (PlayerGroup group : groups) {
            if (group.getPlayers().containsKey(target)) {
                return group.getPlayers().get(target);
            }
        }
        return null;
    }

    /**
     * Processes player join.
     *
     * @param   player
     *          Player who joined
     */
    public void onJoin(@NotNull TabPlayer player) {
        tick();
    }
}
