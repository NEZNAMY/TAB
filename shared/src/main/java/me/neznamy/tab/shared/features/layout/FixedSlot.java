package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.platform.tablist.TabList;
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
                true,
                ping,
                0,
                IChatBaseComponent.optimizedComponent(viewer.getProperty(propertyName).updateAndGet()) // maybe just get is fine?
        );
    }
}