package me.neznamy.tab.platforms.proxy.velocity;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.StateRegistry.PacketMapping;
import com.velocitypowered.proxy.protocol.StateRegistry.PacketRegistry;

import me.neznamy.tab.platforms.proxy.velocity.protocol.ScoreboardDisplay;
import me.neznamy.tab.platforms.proxy.velocity.protocol.ScoreboardObjective;
import me.neznamy.tab.platforms.proxy.velocity.protocol.ScoreboardScore;
import me.neznamy.tab.platforms.proxy.velocity.protocol.Team;

/**
 * Util to register scoreboard packets which are missing on velocity
 */
public class VelocityPacketRegistry {

	private static Method map;
	
	public static boolean registerPackets() {
		try {
			Method register = null;
			for (Method m : PacketRegistry.class.getDeclaredMethods()) {
				if (m.getName().equals("register")) register = m;
			}
			register.setAccessible(true);
			map = StateRegistry.class.getDeclaredMethod("map", int.class, ProtocolVersion.class, boolean.class);
			map.setAccessible(true);

			Supplier<ScoreboardDisplay> display = ScoreboardDisplay::new;
			register.invoke(StateRegistry.PLAY.clientbound, ScoreboardDisplay.class, display, 
					new PacketMapping[] {
							map(0x3D, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x38, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x3A, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x3B, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x3E, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x42, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x43, ProtocolVersion.MINECRAFT_1_15, false)
			});
			Supplier<ScoreboardObjective> objective = ScoreboardObjective::new;
			register.invoke(StateRegistry.PLAY.clientbound, ScoreboardObjective.class, objective, 
					new PacketMapping[] {
							map(0x3B, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x3F, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x41, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x42, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x45, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x49, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x4A, ProtocolVersion.MINECRAFT_1_15, false)
			});
			Supplier<ScoreboardScore> score = ScoreboardScore::new;
			register.invoke(StateRegistry.PLAY.clientbound, ScoreboardScore.class, score, 
					new PacketMapping[] {
							map(0x3C, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x42, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x44, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x45, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x48, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x4C, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x4D, ProtocolVersion.MINECRAFT_1_15, false)
			});
			Supplier<Team> team = Team::new;
			register.invoke(StateRegistry.PLAY.clientbound, Team.class, team, 
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
	
	public static PacketMapping map(final int id, final ProtocolVersion version, final boolean encodeOnly) throws Exception {
		return (PacketMapping) map.invoke(null, id, version, encodeOnly);
	}
}