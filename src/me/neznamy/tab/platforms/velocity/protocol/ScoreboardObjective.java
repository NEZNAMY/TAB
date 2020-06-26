package me.neznamy.tab.platforms.velocity.protocol;

import java.util.Locale;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;

import io.netty.buffer.ByteBuf;

public class ScoreboardObjective implements MinecraftPacket {

	public String name;
	public String value;
	public HealthDisplay type;
	public byte action;

	public ScoreboardObjective() {
	}

	public ScoreboardObjective(final String name, final String value, final HealthDisplay type, final byte action) {
		this.name = name;
		this.value = value;
		this.type = type;
		this.action = action;
	}

	@Override
	public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
		name = ProtocolUtils.readString(buf);
		if (version.getProtocol() <= ProtocolVersion.MINECRAFT_1_7_6.getProtocol()) {
			value = ProtocolUtils.readString(buf);
		}
		action = buf.readByte();
		if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_8.getProtocol() && (action == 0 || action == 2)) {
			value = ProtocolUtils.readString(buf);
			if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_13.getProtocol()) {
				type = HealthDisplay.values()[ProtocolUtils.readVarInt(buf)];
			} else {
				type = HealthDisplay.fromString(ProtocolUtils.readString(buf));
			}
		}
	}

	@Override
	public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
		ProtocolUtils.writeString(buf, name);
		if (version.getProtocol() <= ProtocolVersion.MINECRAFT_1_7_6.getProtocol()) {
			ProtocolUtils.writeString(buf, value);
		}
		buf.writeByte(action);
		if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_8.getProtocol() && (action == 0 || action == 2)) {
			ProtocolUtils.writeString(buf, value);
			if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_13.getProtocol()) {
				ProtocolUtils.writeVarInt(buf, type.ordinal());
			} else {
				ProtocolUtils.writeString(buf, type.toString());
			}
		}
	}

	@Override
	public boolean handle(MinecraftSessionHandler handler) {
		return false;
	}

	public enum HealthDisplay{

		INTEGER, HEARTS;

		@Override
		public String toString(){
			return super.toString().toLowerCase(Locale.ROOT);
		}

		public static HealthDisplay fromString(String s){
			return valueOf(s.toUpperCase(Locale.ROOT));
		}
	}
}