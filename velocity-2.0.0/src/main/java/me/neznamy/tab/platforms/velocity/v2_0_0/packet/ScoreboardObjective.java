package me.neznamy.tab.platforms.velocity.v2_0_0.packet;

import java.util.Locale;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.network.packet.Packet;
import com.velocitypowered.proxy.network.packet.PacketDirection;
import com.velocitypowered.proxy.network.packet.PacketHandler;
import com.velocitypowered.proxy.network.packet.PacketReader;
import com.velocitypowered.proxy.network.packet.PacketWriter;
import com.velocitypowered.proxy.network.ProtocolUtils;

import io.netty.buffer.ByteBuf;

/**
 * A missing Velocity packet
 */
@SuppressWarnings("deprecation")
public class ScoreboardObjective implements Packet {

	public static final PacketReader<ScoreboardObjective> DECODER = PacketReader.method(ScoreboardObjective::new);
	public static final PacketWriter<ScoreboardObjective> ENCODER = PacketWriter.deprecatedEncode();
	
	public String name;
	public String value;
	public HealthDisplay type;
	public byte action;

	public ScoreboardObjective() {
	}

	public ScoreboardObjective(String name, String value, HealthDisplay type, byte action) {
		this.name = name;
		this.value = value;
		this.type = type;
		this.action = action;
	}

	@Override
	public void decode(ByteBuf buf, PacketDirection direction, ProtocolVersion version) {
		name = ProtocolUtils.readString(buf);
		if (version.protocol() <= ProtocolVersion.MINECRAFT_1_7_6.protocol()) {
			value = ProtocolUtils.readString(buf);
		}
		action = buf.readByte();
		if (version.protocol() >= ProtocolVersion.MINECRAFT_1_8.protocol() && (action == 0 || action == 2)) {
			value = ProtocolUtils.readString(buf);
			if (version.protocol() >= ProtocolVersion.MINECRAFT_1_13.protocol()) {
				type = HealthDisplay.values()[ProtocolUtils.readVarInt(buf)];
			} else {
				type = HealthDisplay.valueOf(ProtocolUtils.readString(buf).toUpperCase(Locale.ROOT));
			}
		}
	}

	@Override
	public void encode(ByteBuf buf, ProtocolVersion version) {
		ProtocolUtils.writeString(buf, name);
		if (version.protocol() <= ProtocolVersion.MINECRAFT_1_7_6.protocol()) {
			ProtocolUtils.writeString(buf, value);
		}
		buf.writeByte(action);
		if (version.protocol() >= ProtocolVersion.MINECRAFT_1_8.protocol() && (action == 0 || action == 2)) {
			ProtocolUtils.writeString(buf, value);
			if (version.protocol() >= ProtocolVersion.MINECRAFT_1_13.protocol()) {
				ProtocolUtils.writeVarInt(buf, type.ordinal());
			} else {
				ProtocolUtils.writeString(buf, type.toString());
			}
		}
	}

	@Override
	public boolean handle(PacketHandler arg0) {
		return false;
	}

	@Override
	public String toString(){
		return "ScoreboardObjective(name=" + name + ", value=" + value + ", type=" + type + ", action=" + action + ")";
	}

	public enum HealthDisplay{

		INTEGER, HEARTS;

		@Override
		public String toString(){
			return super.toString().toLowerCase(Locale.ROOT);
		}
	}
}