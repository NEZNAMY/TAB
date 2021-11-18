package me.neznamy.tab.shared.features.layout;

import java.util.UUID;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TabConstants;

public class FixedSlot extends TabFeature {

	private final Layout layout;
	private final UUID id;
	private final String text;
	private final String propertyName;
	private final Object skin;

	public FixedSlot(Layout layout, int slot, String text, String skin) {
		super(layout.getFeatureName());
		setRefreshDisplayName("Updating fixed slots");
		this.layout = layout;
		this.id = layout.getManager().getUUID(slot);
		this.text = text;
		propertyName = "Layout-" + layout.getName() + "SLOT-" + slot;
		this.skin = layout.getManager().getSkinManager().getSkin(skin);
	}

	public String getText() {
		return text;
	}

	public void sendTo(TabPlayer p) {
		if (!layout.containsViewer(p)) return;
		p.setProperty(this, propertyName, text);
		if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData("", id, skin, 0, EnumGamemode.CREATIVE, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).get()))), TabConstants.PacketCategory.LAYOUT_FIXED_SLOTS);
	}	

	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (!layout.containsViewer(p)) return;
		if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(id, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).updateAndGet()))), TabConstants.PacketCategory.LAYOUT_FIXED_SLOTS);
	}
}