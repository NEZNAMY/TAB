package me.neznamy.tab.platforms.velocity.v2_0_0.packet;

import java.util.Arrays;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.network.ProtocolUtils;
import com.velocitypowered.proxy.network.packet.Packet;
import com.velocitypowered.proxy.network.packet.PacketDirection;
import com.velocitypowered.proxy.network.packet.PacketHandler;
import com.velocitypowered.proxy.network.packet.PacketReader;
import com.velocitypowered.proxy.network.packet.PacketWriter;

import io.netty.buffer.ByteBuf;

/**
 * A missing Velocity packet
 */
@SuppressWarnings("deprecation")
public class Team implements Packet {

	public static final PacketReader<Team> DECODER = PacketReader.method(Team::new);
	public static final PacketWriter<Team> ENCODER = PacketWriter.deprecatedEncode();
	
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
	public void decode(ByteBuf buf, PacketDirection direction, ProtocolVersion version) {
		name = ProtocolUtils.readString(buf);
		mode = buf.readByte();
		if (mode == 0 || mode == 2) {
			displayName = ProtocolUtils.readString(buf);
			if (version.protocol() < ProtocolVersion.MINECRAFT_1_13.protocol()) {
				prefix = ProtocolUtils.readString(buf);
				suffix = ProtocolUtils.readString(buf);
			}
			friendlyFire = buf.readByte();
			if (version.protocol() >= ProtocolVersion.MINECRAFT_1_8.protocol()) {
				nameTagVisibility = ProtocolUtils.readString(buf);
				if (version.protocol() >= ProtocolVersion.MINECRAFT_1_9.protocol()) {
					collisionRule = ProtocolUtils.readString(buf);
				}
				color = ((version.protocol() >= ProtocolVersion.MINECRAFT_1_13.protocol()) ? ProtocolUtils.readVarInt(buf) : buf.readByte());
				if (version.protocol() >= ProtocolVersion.MINECRAFT_1_13.protocol()) {
					prefix = ProtocolUtils.readString(buf);
					suffix = ProtocolUtils.readString(buf);
				}
			}
		}
		if (mode == 0 || mode == 3 || mode == 4) {
			int len = (version.protocol() >= ProtocolVersion.MINECRAFT_1_8.protocol()) ? ProtocolUtils.readVarInt(buf) : buf.readShort();
			players = new String[len];
			for (int i = 0; i < len; ++i) {
				players[i] = ProtocolUtils.readString(buf);
			}
		}
	}

	@Override
	public void encode(ByteBuf buf, ProtocolVersion version) {
		ProtocolUtils.writeString(buf, name);
		buf.writeByte(mode);
		if (mode == 0 || mode == 2) {
			ProtocolUtils.writeString(buf, displayName);
			if (version.protocol() < ProtocolVersion.MINECRAFT_1_13.protocol()) {
				ProtocolUtils.writeString(buf, prefix);
				ProtocolUtils.writeString(buf, suffix);
			}
			buf.writeByte(friendlyFire);
			if (version.protocol() >= ProtocolVersion.MINECRAFT_1_8.protocol()) {
				ProtocolUtils.writeString(buf, nameTagVisibility);
				if (version.protocol() >= ProtocolVersion.MINECRAFT_1_9.protocol()) {
					ProtocolUtils.writeString(buf, collisionRule);
				}
				if (version.protocol() >= ProtocolVersion.MINECRAFT_1_13.protocol()) {
					ProtocolUtils.writeVarInt(buf, color);
					ProtocolUtils.writeString(buf, prefix);
					ProtocolUtils.writeString(buf, suffix);
				} else {
					buf.writeByte(color);
				}
			}
		}
		if (mode == 0 || mode == 3 || mode == 4) {
			if (version.protocol() >= ProtocolVersion.MINECRAFT_1_8.protocol()) {
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
	public boolean handle(PacketHandler arg0) {
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