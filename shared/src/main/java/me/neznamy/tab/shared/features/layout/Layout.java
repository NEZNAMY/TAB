package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.shared.player.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.ServerSwitchListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.player.tablist.TabListEntry;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Layout extends TabFeature implements Refreshable, ServerSwitchListener {

    @Getter private final String featureName = "Layout";
    @Getter private final String refreshDisplayName = "Updating player groups";
    @Getter private final String name;
    @Getter private final LayoutManager manager;
    private final Condition displayCondition;
    private final Map<Integer, FixedSlot> fixedSlots;
    private final List<Integer> emptySlots;
    @Getter private final List<ParentGroup> groups;
    @Getter private final Set<TabPlayer> viewers = Collections.newSetFromMap(new WeakHashMap<>());

    public void sendTo(TabPlayer p) {
        if (viewers.contains(p)) return;
        viewers.add(p);
        List<TabListEntry> list = new ArrayList<>();
        groups.forEach(g -> list.addAll(g.getSlots(p)));
        for (FixedSlot slot : fixedSlots.values()) {
            list.add(slot.createEntry(p));
        }
        for (int slot : emptySlots) {
            list.add(new TabListEntry(manager.getUUID(slot), getEntryName(p, slot), manager.getSkinManager().getDefaultSkin(), true, manager.getEmptySlotPing(), 0, new IChatBaseComponent("")));
        }
        if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
        p.getTabList().addEntries(list);
    }

    public String getEntryName(TabPlayer viewer, long slot) {
        return viewer.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId() ? "|slot_" + (10+slot) : "";
    }

    public void removeFrom(TabPlayer p) {
        if (!viewers.contains(p)) return;
        viewers.remove(p);
        if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
        p.getTabList().removeEntries(manager.getUuids().values());
    }

    public boolean isConditionMet(TabPlayer p) {
        return displayCondition == null || displayCondition.isMet(p);
    }

    public void tick() {
        Stream<TabPlayer> str = manager.getSortedPlayers().keySet().stream();
        if (manager.isHideVanishedPlayers()) {
            str = str.filter(player -> !player.isVanished());
        }
        List<TabPlayer> players = str.collect(Collectors.toList());
        for (ParentGroup group : groups) {
            group.tick(players);
        }
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        tick();
    }

    public boolean containsViewer(TabPlayer viewer) {
        return viewers.contains(viewer);
    }

    public PlayerSlot getSlot(TabPlayer p) {
        for (ParentGroup group : groups) {
            if (group.getPlayers().containsKey(p)) {
                return group.getPlayers().get(p);
            }
        }
        return null;
    }

    @Override
    public void onServerChange(TabPlayer changed, String from, String to) {
        if (viewers.remove(changed)) sendTo(changed);
    }
}