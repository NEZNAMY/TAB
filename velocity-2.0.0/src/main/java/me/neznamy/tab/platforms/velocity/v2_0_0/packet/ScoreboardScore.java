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
public class ScoreboardScore implements Packet {

	public static final PacketReader<ScoreboardScore> DECODER = PacketReader.method(ScoreboardScore::new);
	public static final PacketWriter<ScoreboardScore> ENCODER = PacketWriter.deprecatedEncode();
	
	private String itemName;
	private byte action;
	private String scoreName;
	private int value;

	public ScoreboardScore() {
	}

	public ScoreboardScore(String itemName, byte action, String scoreName, int value) {
		this.itemName = itemName;
		this.action = action;
		this.scoreName = scoreName;
		this.value = value;
	}

	@Override
	public void decode(ByteBuf buf, PacketDirection direction, ProtocolVersion version) {
		itemName = ProtocolUtils.readString(buf);
		action = buf.readByte();
		if (version.protocol() >= ProtocolVersion.MINECRAFT_1_8.protocol()) {
			scoreName = ProtocolUtils.readString(buf);
			if (action != 1) {
				value = ProtocolUtils.readVarInt(buf);
			}
		} else if (action != 1) {
			scoreName = ProtocolUtils.readString(buf);
			value = buf.readInt();
		}
	}

	@Override
	public void encode(ByteBuf buf, ProtocolVersion version) {
		ProtocolUtils.writeString(buf, itemName);
		buf.writeByte(action);
		if (version.protocol() >= ProtocolVersion.MINECRAFT_1_8.protocol()) {
			ProtocolUtils.writeString(buf, scoreName);
			if (action != 1) {
				ProtocolUtils.writeVarInt(buf, value);
			}
		} else if (action != 1) {
			ProtocolUtils.writeString(buf, scoreName);
			buf.writeInt(value);
		}
	}
	
	@Override
	public boolean handle(PacketHandler arg0) {
		return false;
	}

	@Override
	public String toString(){
		return "ScoreboardScore(itemName=" + itemName + ", action=" + action + ", scoreName=" + scoreName + ", value=" + value + ")";
	}
}