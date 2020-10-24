package me.neznamy.tab.platforms.proxy.velocity.protocol;

import java.util.Arrays;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;

import io.netty.buffer.ByteBuf;

/**
 * A missing Velocity packet
 */
public class Team implements MinecraftPacket {

	public String name;
	public byte mode;
	public String displayName;
	public String prefix;
	public String suffix;
	public String nameTagVisibility;
	public String collisionRule;
	public int color;
	public byte friendlyFire;
	public String[] players;

	public Team() {
	}

	public Team(String name, byte mode, String displayName, String prefix, String suffix, String nameTagVisibility, String collisionRule, int color, byte friendlyFire, String[] players){
		this.name = name;
		this.mode = mode;
		this.displayName = displayName;
		this.prefix = prefix;
		this.suffix = suffix;
		this.nameTagVisibility = nameTagVisibility;
		this.collisionRule = collisionRule;
		this.color = color;
		this.friendlyFire = friendlyFire;
		this.players = players;
	}

	@Override
	public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
		name = ProtocolUtils.readString(buf);
		mode = buf.readByte();
		if (mode == 0 || mode == 2) {
			displayName = ProtocolUtils.readString(buf);
			if (version.getProtocol() < ProtocolVersion.MINECRAFT_1_13.getProtocol()) {
				prefix = ProtocolUtils.readString(buf);
				suffix = ProtocolUtils.readString(buf);
			}
			friendlyFire = buf.readByte();
			if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_8.getProtocol()) {
				nameTagVisibility = ProtocolUtils.readString(buf);
				if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_9.getProtocol()) {
					collisionRule = ProtocolUtils.readString(buf);
				}
				color = ((version.getProtocol() >= ProtocolVersion.MINECRAFT_1_13.getProtocol()) ? ProtocolUtils.readVarInt(buf) : buf.readByte());
				if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_13.getProtocol()) {
					prefix = ProtocolUtils.readString(buf);
					suffix = ProtocolUtils.readString(buf);
				}
			}
		}
		if (mode == 0 || mode == 3 || mode == 4) {
			int len = (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_8.getProtocol()) ? ProtocolUtils.readVarInt(buf) : buf.readShort();
			players = new String[len];
			for (int i = 0; i < len; ++i) {
				players[i] = ProtocolUtils.readString(buf);
			}
		}
	}

	@Override
	public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
		ProtocolUtils.writeString(buf, name);
		buf.writeByte(mode);
		if (mode == 0 || mode == 2) {
			ProtocolUtils.writeString(buf, displayName);
			if (version.getProtocol() < ProtocolVersion.MINECRAFT_1_13.getProtocol()) {
				ProtocolUtils.writeString(buf, prefix);
				ProtocolUtils.writeString(buf, suffix);
			}
			buf.writeByte(friendlyFire);
			if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_8.getProtocol()) {
				ProtocolUtils.writeString(buf, nameTagVisibility);
				if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_9.getProtocol()) {
					ProtocolUtils.writeString(buf, collisionRule);
				}
				if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_13.getProtocol()) {
					ProtocolUtils.writeVarInt(buf, color);
					ProtocolUtils.writeString(buf, prefix);
					ProtocolUtils.writeString(buf, suffix);
				} else {
					buf.writeByte(color);
				}
			}
		}
		if (mode == 0 || mode == 3 || mode == 4) {
			if (version.getProtocol() >= ProtocolVersion.MINECRAFT_1_8.getProtocol()) {
				ProtocolUtils.writeVarInt(buf, players.length);
			} else {
				buf.writeShort(players.length);
			}
			for (String player : players) {
				ProtocolUtils.writeString(buf, player);
			}
		}
	}

	@Override
	public boolean handle(MinecraftSessionHandler handler) {
		return false;
	}
	public int getFriendlyFire() {
		return friendlyFire;
	}
	public String[] getPlayers() {
		return players;
	}
	
	@Override
	public String toString(){
		return "Team(name=" + name + ", mode=" + mode + ", displayName=" + displayName + ", prefix=" + prefix + ", suffix=" + suffix + ", nameTagVisibility=" + nameTagVisibility + ", collisionRule=" + collisionRule + ", color=" + color + ", friendlyFire=" + friendlyFire + ", players=" + Arrays.deepToString(players) + ")";
	}
}