package me.neznamy.tab.platforms.velocity.protocol;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;

import io.netty.buffer.ByteBuf;

/**
 * A missing Velocity packet
 */
public class ScoreboardScore implements MinecraftPacket {

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
	public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
		itemName = ProtocolUtils.readString(buf);
		action = buf.readByte();
		if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_8.getProtocol()) {
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
	public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
		ProtocolUtils.writeString(buf, itemName);
		buf.writeByte(action);
		if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_8.getProtocol()) {
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
	public boolean handle(MinecraftSessionHandler handler) {
		return false;
	}

	@Override
	public String toString(){
		return "ScoreboardScore(itemName=" + itemName + ", action=" + action + ", scoreName=" + scoreName + ", value=" + value + ")";
	}
}