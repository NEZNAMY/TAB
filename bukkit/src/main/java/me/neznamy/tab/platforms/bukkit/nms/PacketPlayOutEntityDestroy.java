package me.neznamy.tab.platforms.bukkit.nms;

import java.util.Arrays;

import me.neznamy.tab.api.protocol.TabPacket;

public class PacketPlayOutEntityDestroy implements TabPacket {

	private final int[] entities;

	public PacketPlayOutEntityDestroy(int... entities) {
		this.entities = entities;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutEntityDestroy{entities=%s}", Arrays.toString(entities));
	}

	public int[] getEntities() {
		return entities;
	}
}