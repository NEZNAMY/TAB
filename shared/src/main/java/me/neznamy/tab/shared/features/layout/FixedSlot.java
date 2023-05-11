package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public class FixedSlot extends TabFeature implements Refreshable {

    @Getter private final String featureName = "Layout";
    @Getter private final String refreshDisplayName = "Updating fixed slots";

    private final Layout layout;
    private final UUID id;
    private final String text;
    private final String propertyName;
    private final TabList.Skin skin;
    private final int ping;

    @Override
    public void refresh(@NonNull TabPlayer p, boolean force) {
        if (!layout.containsViewer(p) || p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
        p.getTabList().updateDisplayName(id, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).updateAndGet()));
    }

    public @NotNull TabList.Entry createEntry(@NonNull TabPlayer viewer) {
        viewer.setProperty(this, propertyName, text);
        return new TabList.Entry(
                id,
                layout.getEntryName(viewer, id.getLeastSignificantBits()),
                skin,
                ping,
                0,
                IChatBaseComponent.optimizedComponent(viewer.getProperty(propertyName).updateAndGet()) // maybe just get is fine?
        );
    }

    public static void registerFromLine(@NonNull LayoutManager manager, @NonNull Layout layout, @NonNull String line) {
        String[] array = line.split("\\|");
        if (array.length < 2) {
            TAB.getInstance().getMisconfigurationHelper().invalidFixedSlotDefinition(layout.getName(), line);
            return;
        }
        int slot;
        try {
            slot = Integer.parseInt(array[0]);
        } catch (NumberFormatException e) {
            TAB.getInstance().getMisconfigurationHelper().invalidFixedSlotDefinition(layout.getName(), line);
            return;
        }
        String text = array[1];
        String skin = array.length > 2 ? array[2] : "";
        int ping = array.length > 3 ? TAB.getInstance().getErrorManager().parseInteger(array[3], manager.getEmptySlotPing()) : manager.getEmptySlotPing();
        FixedSlot f = new FixedSlot(layout, manager.getUUID(slot), text,
                "Layout-" + layout.getName() + "SLOT-" + slot,
                manager.getSkinManager().getSkin(skin.length() == 0 ? manager.getDefaultSkin() : skin), ping);
        layout.getEmptySlots().remove((Integer)slot);
        if (text.length() > 0) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layoutSlot(layout.getName(), slot), f);
        layout.getFixedSlots().put(slot, f);
    }
}