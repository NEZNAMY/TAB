package me.neznamy.tab.shared.packets;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.shared.ProtocolVersion;

public abstract class UniversalPacketPlayOut extends PacketPlayOut{

	public abstract Object toBungee(ProtocolVersion clientVersion);
	public abstract Object toVelocity(ProtocolVersion clientVersion);
}