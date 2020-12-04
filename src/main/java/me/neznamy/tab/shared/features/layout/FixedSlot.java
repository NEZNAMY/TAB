package me.neznamy.tab.shared.features.layout;

import me.neznamy.tab.api.TabPlayer;

public class FixedSlot {

	private int slot;
	private String text;
	private String skin;
	
	public FixedSlot(int slot, String text, String skin) {
		this.slot = slot;
		this.text = text;
		this.skin = skin;
	}
	
	public void onJoin(TabPlayer p) {
		p.setProperty("SLOT-" + slot, text);
	}	
	
	public int getSlot() {
		return slot;
	}
	
	public String getText(TabPlayer viewer) {
		return viewer.getProperty("SLOT-" + this.slot).get();
	}
	
	public String getSkin() {
		return skin;
	}
}