package me.neznamy.tab.shared.features.layout;

import java.util.UUID;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.TabConstants;

public class FixedSlot extends TabFeature {

    private final Layout layout;
    private final UUID id;
    private final String text;
    private final String propertyName;
    private final Skin skin;
    private final int ping;

    public FixedSlot(Layout layout, int slot, String text, String skin, int ping) {
        super(layout.getFeatureName(), "Updating fixed slots");
        this.layout = layout;
        this.id = layout.getManager().getUUID(slot);
        this.text = text;
        propertyName = "Layout-" + layout.getName() + "SLOT-" + slot;
        this.skin = layout.getManager().getSkinManager().getSkin(skin.length() == 0 ? layout.getManager().getDefaultSkin() : skin);
        this.ping = ping;
    }

    public String getText() {
        return text;
    }

    public UUID getId() {
        return id;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Skin getSkin() {
        return skin;
    }

    public int getPing() {
        return ping;
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (!layout.containsViewer(p) || p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
        p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(id, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).updateAndGet()))), TabConstants.PacketCategory.LAYOUT_FIXED_SLOTS);
    }
}