package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public class FixedSlot extends TabFeature implements Refreshable {

    private final LayoutManagerImpl manager;
    @Getter private final int slot;
    private final LayoutPattern pattern;
    private final UUID id;
    private final String text;
    private final String propertyName;
    private final String skin;
    private final String skinProperty;
    private final int ping;

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (!manager.getViews().containsKey(p) || manager.getViews().get(p).getPattern() != pattern ||
                p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
        if (p.getProperty(skinProperty).update()) {
            p.getTabList().removeEntry(id);
            p.getTabList().addEntry(createEntry(p));
        } else {
            p.getTabList().updateDisplayName(id, TabComponent.optimized(p.getProperty(propertyName).updateAndGet()));
        }
    }

    @Override
    @NotNull
    public String getRefreshDisplayName() {
        return "Updating fixed slots";
    }

    public @NotNull TabList.Entry createEntry(@NotNull TabPlayer viewer) {
        viewer.setProperty(this, propertyName, text);
        viewer.setProperty(this, skinProperty, skin);
        return new TabList.Entry(
                id,
                manager.getDirection().getEntryName(viewer, slot),
                manager.getSkinManager().getSkin(viewer.getProperty(skinProperty).updateAndGet()),
                ping,
                0,
                TabComponent.optimized(viewer.getProperty(propertyName).updateAndGet())
        );
    }

    public static @Nullable FixedSlot fromLine(@NotNull String line, @NotNull LayoutPattern pattern, @NotNull LayoutManagerImpl manager) {
        String[] array = line.split("\\|");
        if (array.length < 1) {
            TAB.getInstance().getConfigHelper().startup().invalidFixedSlotDefinition(pattern.getName(), line);
            return null;
        }
        int slot;
        try {
            slot = Integer.parseInt(array[0]);
        } catch (NumberFormatException e) {
            TAB.getInstance().getConfigHelper().startup().invalidFixedSlotDefinition(pattern.getName(), line);
            return null;
        }
        String text = array.length > 1 ? array[1] : "";
        String skin = array.length > 2 ? array[2] : "";
        int ping = manager.getEmptySlotPing();
        if (array.length > 3) {
            try {
                ping = (int) Math.round(Double.parseDouble(array[3]));
            } catch (NumberFormatException ignored) {
                // Maybe a warning?
            }
        }
        FixedSlot f = new FixedSlot(
                manager,
                slot,
                pattern,
                manager.getUUID(slot),
                text,
                "Layout-" + pattern.getName() + "-SLOT-" + slot,
                skin.isEmpty() ? manager.getDefaultSkin(slot) : skin,
                "Layout-" + pattern.getName() + "-SLOT-" + slot + "-skin",
                ping
        );
        if (!text.isEmpty()) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layoutSlot(pattern.getName(), slot), f);
        return f;
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return manager.getFeatureName();
    }
}