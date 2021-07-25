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

	private UUID id;
	private int slot;
	private String text;

	public FixedSlot(int slot, String text) {
		super("Tablist layout");
		this.id = UUID.randomUUID();
		this.slot = slot;
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void load() {
		TAB.getInstance().getOnlinePlayers().forEach(p -> onJoin(p));
	}

	@Override
	public void onJoin(TabPlayer p) {
		p.setProperty(this, "SLOT-" + slot, text);
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData((char)1 + String.format("SLOT%02d", slot), id, null, 0, EnumGamemode.CREATIVE, IChatBaseComponent.optimizedComponent(p.getProperty("SLOT-" + slot).get()))), this);
	}	

	@Override
	public void refresh(TabPlayer p, boolean force) {
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(id, IChatBaseComponent.optimizedComponent(p.getProperty("SLOT-" + slot).updateAndGet()))), this);
	}
	
	@Override
	public void unload() {
		TAB.getInstance().getOnlinePlayers().forEach(p -> p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(id)), this));
	}
}