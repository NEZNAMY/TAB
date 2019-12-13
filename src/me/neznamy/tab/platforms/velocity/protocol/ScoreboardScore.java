package me.neznamy.tab.platforms.velocity.protocol;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;

import io.netty.buffer.ByteBuf;

public class ScoreboardScore implements MinecraftPacket
{

	private String itemName;
	private byte action;
	private String scoreName;
	private int value;

	public ScoreboardScore() {
	}

	public ScoreboardScore(final String itemName, final byte action, final String scoreName, final int value) {
		this.itemName = itemName;
		this.action = action;
		this.scoreName = scoreName;
		this.value = value;
	}

	@Override
	public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
		itemName = ProtocolUtils.readString(buf);
		action = buf.readByte();
		scoreName = ProtocolUtils.readString(buf);
		if (action != 1){
			value = ProtocolUtils.readVarInt(buf);
		}
	}

	@Override
	public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
		ProtocolUtils.writeString(buf, itemName);
		buf.writeByte(action);
		ProtocolUtils.writeString(buf, scoreName);
		if (action != 1){
			ProtocolUtils.writeVarInt(buf, value);
		}
	}

	@Override
	public boolean handle(MinecraftSessionHandler handler) {
		return false;
	}
}