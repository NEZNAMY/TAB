package me.neznamy.tab.shared.features.layout;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.TabConstants;

@RequiredArgsConstructor
public class FixedSlot extends TabFeature {

    @Getter private final String featureName = "Layout";
    @Getter private final String refreshDisplayName = "Updating fixed slots";

    private final Layout layout;
    private final UUID id;
    private final String text;
    private final String propertyName;
    private final Skin skin;
    private final int ping;

    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (!layout.containsViewer(p) || p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
        p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
                new PlayerInfoData(id, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).updateAndGet()))));
    }

    public PlayerInfoData createEntry(TabPlayer viewer) {
        viewer.setProperty(this, propertyName, text);
        return new PlayerInfoData(
                layout.getEntryName(viewer, id.getLeastSignificantBits()),
                id,
                skin,
                true,
                ping,
                PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE,
                IChatBaseComponent.optimizedComponent(viewer.getProperty(propertyName).updateAndGet()), // maybe just get is fine?
                null,
                null
        );
    }
}