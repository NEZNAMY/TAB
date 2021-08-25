package me.neznamy.tab.shared.features.layout;

import java.util.UUID;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.CpuConstants;
import me.neznamy.tab.shared.TAB;

public class FixedSlot extends TabFeature {

	private Layout layout;
	private UUID id;
	private int slot;
	private String text;
	private String propertyName;
	private Object skin;

	public FixedSlot(Layout layout, int slot, String text, String skin) {
		super(layout.getFeatureName());
		this.layout = layout;
		this.id = layout.getUUID(slot);
		this.slot = slot;
		this.text = text;
		propertyName = "SLOT-" + slot;
		this.skin = layout.getSkinManager().getSkin(skin);
	}

	public String getText() {
		return text;
	}

	@Override
	public void load() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			onJoin(p);
		}
	}

	@Override
	public void onJoin(TabPlayer p) {
		p.setProperty(this, propertyName, text);
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(layout.formatSlot(slot), id, skin, 0, EnumGamemode.CREATIVE, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).get()))), CpuConstants.PacketCategory.LAYOUT_FIXED_SLOTS);
	}	

	@Override
	public void refresh(TabPlayer p, boolean force) {
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(id, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).updateAndGet()))), CpuConstants.PacketCategory.LAYOUT_FIXED_SLOTS);
	}

	@Override
	public void unload() {
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(id));
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			p.sendCustomPacket(packet, CpuConstants.PacketCategory.LAYOUT_FIXED_SLOTS);
		}
	}
}