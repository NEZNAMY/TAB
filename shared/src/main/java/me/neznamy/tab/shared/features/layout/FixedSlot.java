package me.neznamy.tab.shared.features.layout;

import java.util.UUID;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;

public class FixedSlot extends TabFeature {

	private Layout layout;
	private UUID id;
	private int slot;
	private String text;
	private String propertyName;

	public FixedSlot(Layout layout, UUID id, int slot, String text) {
		super("Tablist layout");
		this.layout = layout;
		this.id = id;
		this.slot = slot;
		this.text = text;
		propertyName = "SLOT-" + slot;
	}

	public String getText() {
		return text;
	}

	@Override
	public void load() {
		TAB.getInstance().getOnlinePlayers().forEach(this::onJoin);
	}

	@Override
	public void onJoin(TabPlayer p) {
		p.setProperty(this, propertyName, text);
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(layout.formatSlot(slot), id, null, 0, EnumGamemode.CREATIVE, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).get()))), this);
	}	

	@Override
	public void refresh(TabPlayer p, boolean force) {
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(id, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).updateAndGet()))), this);
	}

	@Override
	public void unload() {
		TAB.getInstance().getOnlinePlayers().forEach(p -> p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(id)), this));
	}
}