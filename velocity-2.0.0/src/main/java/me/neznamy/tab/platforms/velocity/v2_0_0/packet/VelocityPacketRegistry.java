package me.neznamy.tab.platforms.velocity.v2_0_0.packet;

import java.lang.reflect.Method;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.network.StateRegistry;
import com.velocitypowered.proxy.network.StateRegistry.PacketMapping;
import com.velocitypowered.proxy.network.StateRegistry.PacketRegistry;

/**
 * Util to register scoreboard packets which are missing on velocity
 */
public class VelocityPacketRegistry {

	//packet id mapping method
	private Method map;
	
	/**
	 * Registers missing velocity packets
	 * @return true if registration was successful, false if not
	 */
	public boolean registerPackets() {
		try {
			Method register = null;
			for (Method m : PacketRegistry.class.getDeclaredMethods()) {
				if (m.getName().equals("register")) register = m;
			}
			register.setAccessible(true);
			map = StateRegistry.class.getDeclaredMethod("map", int.class, ProtocolVersion.class, boolean.class);
			map.setAccessible(true);
			register.invoke(StateRegistry.PLAY.clientbound, ScoreboardDisplay.class, Team.DECODER, Team.ENCODER, 
					new PacketMapping[] {
							map(0x3D, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x38, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x3A, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x3B, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x3E, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x42, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x43, ProtocolVersion.MINECRAFT_1_15, false)
			});
			register.invoke(StateRegistry.PLAY.clientbound, ScoreboardObjective.class, Team.DECODER, Team.ENCODER, 
					new PacketMapping[] {
							map(0x3B, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x3F, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x41, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x42, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x45, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x49, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x4A, ProtocolVersion.MINECRAFT_1_15, false)
			});
			register.invoke(StateRegistry.PLAY.clientbound, ScoreboardScore.class, Team.DECODER, Team.ENCODER, 
					new PacketMapping[] {
							map(0x3C, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x42, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x44, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x45, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x48, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x4C, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x4D, ProtocolVersion.MINECRAFT_1_15, false)
			});
			register.invoke(StateRegistry.PLAY.clientbound, Team.class, Team.DECODER, Team.ENCODER, 
					new PacketMapping[] {
							map(0x3E, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x41, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x43, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x44, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x47, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x4B, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x4C, ProtocolVersion.MINECRAFT_1_15, false)
			});
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Calls map method and returns the output packet mapping
	 * @param id - packet id
	 * @param version - client version
	 * @param encodeOnly - no idea
	 * @return result from map method
	 * @throws Exception - if reflection fails
	 */
	private PacketMapping map(int id, ProtocolVersion version, boolean encodeOnly) throws Exception {
		return (PacketMapping) map.invoke(null, id, version, encodeOnly);
	}
}