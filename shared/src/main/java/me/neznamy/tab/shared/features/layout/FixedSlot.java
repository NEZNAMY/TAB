package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A fixed layout slot with defined slot, text and maybe also ping and skin.
 */
public class FixedSlot extends RefreshableFeature {

    private static final StringToComponentCache cache = new StringToComponentCache("LayoutFixedSlot", 1000);

    @NonNull private final LayoutManagerImpl manager;
    @Getter private final int slot;
    @NonNull private final LayoutPattern pattern;
    @NonNull private final UUID id;
    @NonNull private final String text;
    @NonNull private final String propertyName;
    @NonNull private final String skin;
    @NonNull private final String skinProperty;
    private final int ping;

    public FixedSlot(@NonNull LayoutManagerImpl manager, int slot, @NonNull LayoutPattern pattern, @NonNull UUID id,
                     @NonNull String text, @NonNull String propertyName, @NonNull String skin, @NonNull String skinProperty, int ping) {
        super(manager.getFeatureName(), "Updating fixed slots");
        this.manager = manager;
        this.slot = slot;
        this.pattern = pattern;
        this.id = id;
        this.text = text;
        this.propertyName = propertyName;
        this.skin = skin;
        this.skinProperty = skinProperty;
        this.ping = ping;
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.layoutData.view == null || p.layoutData.view.getPattern() != pattern ||
                p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return; // TODO check if / make view null for <1.8 and bedrock to skip all these checks everywhere
        if (p.getProperty(skinProperty).update()) {
            p.getTabList().removeEntry(id);
            p.getTabList().addEntry(createEntry(p));
        } else {
            p.getTabList().updateDisplayName(id, cache.get(p.getProperty(propertyName).updateAndGet()));
        }
    }

    /**
     * Creates a tablist entry from this slot for given viewer.
     *
     * @param   viewer
     *          Player viewing the slot
     * @return  Tablist entry from this slot
     */
    public @NotNull TabList.Entry createEntry(@NotNull TabPlayer viewer) {
        viewer.setProperty(this, propertyName, text);
        viewer.setProperty(this, skinProperty, skin);
        return new TabList.Entry(
                id,
                manager.getDirection().getEntryName(viewer, slot),
                manager.getSkinManager().getSkin(viewer.getProperty(skinProperty).updateAndGet()),
                true,
                ping,
                0,
                cache.get(viewer.getProperty(propertyName).updateAndGet())
        );
    }

    /**
     * Creates a new instance with given parameters. It may return {@code null} if pattern is invalid.
     *
     * @param   line
     *          Line definition
     * @param   pattern
     *          Layout this slot belongs to
     * @param   manager
     *          Layout manager
     * @return  New slot using given line or {@code null} if invalid
     */
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
}