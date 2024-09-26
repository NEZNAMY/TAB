package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.files.config.LayoutConfiguration.LayoutDefinition.FixedSlotDefinition;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A fixed layout slot with defined slot, text and maybe also ping and skin.
 */
@RequiredArgsConstructor
public class FixedSlot extends RefreshableFeature {

    @NotNull private static final StringToComponentCache cache = new StringToComponentCache("LayoutFixedSlot", 1000);

    @NonNull private final LayoutManagerImpl manager;
    @Getter private final int slot;
    @NonNull private final LayoutPattern pattern;
    @NonNull private final UUID id;
    @NonNull private final String text;
    @NonNull private final String skin;
    private final int ping;

    @NotNull
    @Override
    public String getFeatureName() {
        return manager.getFeatureName();
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating fixed slots";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.layoutData.currentLayout == null || p.layoutData.currentLayout.view.getPattern() != pattern ||
                p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return; // TODO check if / make view null for <1.8 and bedrock to skip all these checks everywhere
        if (p.layoutData.currentLayout.fixedSlotSkins.get(this).update()) {
            p.getTabList().removeEntry(id);
            p.getTabList().addEntry(createEntry(p));
        } else {
            p.getTabList().updateDisplayName(id, cache.get(p.layoutData.currentLayout.fixedSlotTexts.get(this).updateAndGet()));
        }
    }

    /**
     * Creates a tablist entry from this slot for given viewer.
     *
     * @param   viewer
     *          Player viewing the slot
     * @return  Tablist entry from this slot
     */
    @NotNull
    public TabList.Entry createEntry(@NotNull TabPlayer viewer) {
        viewer.layoutData.currentLayout.fixedSlotTexts.put(this, new Property(this, viewer, text));
        viewer.layoutData.currentLayout.fixedSlotSkins.put(this, new Property(this, viewer, skin));
        return new TabList.Entry(
                id,
                manager.getConfiguration().direction.getEntryName(viewer, slot, LayoutManagerImpl.isTeamsEnabled()),
                manager.getSkinManager().getSkin(viewer.layoutData.currentLayout.fixedSlotSkins.get(this).updateAndGet()),
                true,
                ping,
                0,
                cache.get(viewer.layoutData.currentLayout.fixedSlotTexts.get(this).updateAndGet()),
                Integer.MAX_VALUE - manager.getConfiguration().direction.translateSlot(slot)
        );
    }

    /**
     * Creates a new instance with given parameters.
     *
     * @param   def
     *          Fixed slot definition
     * @param   pattern
     *          Layout this slot belongs to
     * @param   manager
     *          Layout manager
     * @return  New slot using given line or {@code null} if invalid
     */
    @NotNull
    public static FixedSlot fromDefinition(@NotNull FixedSlotDefinition def, @NotNull LayoutPattern pattern, @NotNull LayoutManagerImpl manager) {
        FixedSlot f = new FixedSlot(
                manager,
                def.slot,
                pattern,
                manager.getUUID(def.slot),
                def.text,
                def.skin == null || def.skin.isEmpty() ? manager.getConfiguration().getDefaultSkin(def.slot) : def.skin,
                def.ping == null ? manager.getConfiguration().emptySlotPing : def.ping
        );
        if (!def.text.isEmpty()) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layoutSlot(pattern.getName(), def.slot), f);
        return f;
    }
}