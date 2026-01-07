package me.neznamy.tab.shared.features.layout.impl.common;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.layout.pattern.FixedSlotPattern;
import me.neznamy.tab.shared.features.layout.pattern.LayoutPattern;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Nullable private final String pingText;
    private final int defaultPing;

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
        if (p.layoutData.currentLayout == null || p.layoutData.currentLayout.view.getPattern() != pattern) return;
        if (p.layoutData.currentLayout.fixedSlotSkins.get(this).update()) {
            p.getTabList().removeEntry(id);
            p.getTabList().addEntry(createEntry(p));
            return;
        }
        if (p.layoutData.currentLayout.fixedSlotTexts.get(this).update()) {
            p.getTabList().updateDisplayName(id, cache.get(p.layoutData.currentLayout.fixedSlotTexts.get(this).get()));
        }
        Property pingProperty = p.layoutData.currentLayout.fixedSlotPings.get(this);
        if (pingProperty != null && pingProperty.update()) {
            p.getTabList().updateLatency(id, parsePing(pingProperty.get(), p));
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
        int ping = defaultPing;
        if (pingText != null) {
            Property pingProperty = new Property(this, viewer, pingText);
            viewer.layoutData.currentLayout.fixedSlotPings.put(this, pingProperty);
            ping = parsePing(pingProperty.updateAndGet(), viewer);
        }
        return new TabList.Entry(
                id,
                manager.getConfiguration().getDirection().getEntryName(viewer, slot, LayoutManagerImpl.isTeamsEnabled()),
                manager.getSkinManager().getSkin(viewer.layoutData.currentLayout.fixedSlotSkins.get(this).updateAndGet()),
                true,
                ping,
                0,
                cache.get(viewer.layoutData.currentLayout.fixedSlotTexts.get(this).updateAndGet()),
                Integer.MAX_VALUE - manager.getConfiguration().getDirection().translateSlot(slot),
                true
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
    public static FixedSlot fromDefinition(@NotNull FixedSlotPattern def, @NotNull LayoutPattern pattern, @NotNull LayoutManagerImpl manager) {
        String skin;
        if (def.getSkin() != null && !def.getSkin().isEmpty()) {
            skin = def.getSkin();
        } else if (pattern.getDefaultSkinOverride() != null) {
            skin = pattern.getDefaultSkinOverride();
        } else {
            skin = manager.getConfiguration().getDefaultSkin(def.getSlot());
        }
        FixedSlot f = new FixedSlot(
                manager,
                def.getSlot(),
                pattern,
                manager.getUUID(def.getSlot()),
                def.getText(),
                skin,
                def.getPing(),
                manager.getConfiguration().getEmptySlotPing()
        );
        if (!def.getText().isEmpty()) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layoutSlot(pattern.getName(), def.getSlot()), f);
        return f;
    }

    private int parsePing(@NotNull String value, @NotNull TabPlayer viewer) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return defaultPing;
        try {
            return (int) Math.round(Double.parseDouble(trimmed));
        } catch (NumberFormatException ignored) {
            if (viewer instanceof ProxyTabPlayer && !((ProxyTabPlayer)viewer).isBridgeConnected()) return defaultPing;
            if (pingText != null && pingText.contains("%")) {
                TAB.getInstance().getConfigHelper().runtime().error(String.format(
                        "Placeholder \"%s\" used as fixed slot ping in layout \"%s\" (slot %d) returned \"%s\" for player %s, which is not a valid number.",
                        pingText, pattern.getName(), slot, trimmed, viewer.getName()
                ));
            } else {
                TAB.getInstance().getConfigHelper().runtime().error(String.format(
                        "Fixed slot ping \"%s\" in layout \"%s\" (slot %d) is not a valid number. Using default ping value.",
                        pingText == null ? value : pingText, pattern.getName(), slot
                ));
            }
            return defaultPing;
        }
    }
}