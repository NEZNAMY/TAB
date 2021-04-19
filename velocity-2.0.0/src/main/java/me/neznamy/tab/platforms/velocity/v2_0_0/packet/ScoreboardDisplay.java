package me.neznamy.tab.platforms.velocity.v2_0_0.packet;

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
public class ScoreboardDisplay implements Packet {

	public static final PacketReader<ScoreboardDisplay> DECODER = PacketReader.method(ScoreboardDisplay::new);
	public static final PacketWriter<ScoreboardDisplay> ENCODER = PacketWriter.deprecatedEncode();

	public byte position;
	public String name;

	public ScoreboardDisplay() {
	}

	public ScoreboardDisplay(byte position, String name) {
		this.position = position;
		this.name = name;
	}

	@Override
	public void decode(ByteBuf buf, PacketDirection direction, ProtocolVersion version) {
		position = buf.readByte();
		name = ProtocolUtils.readString(buf);
	}

	@Override
	public void encode(ByteBuf buf, ProtocolVersion version) {
		buf.writeByte(position);
		ProtocolUtils.writeString(buf, name);
	}

	@Override
	public String toString(){
		return "ScoreboardDisplay(position=" + position + ", name=" + name + ")";
	}

	@Override
	public boolean handle(PacketHandler arg0) {
		return false;
	}
}