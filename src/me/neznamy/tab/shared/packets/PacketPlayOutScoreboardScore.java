package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.bukkit.packets.EnumConstant;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.md_5.bungee.protocol.packet.ScoreboardScore;

public class PacketPlayOutScoreboardScore extends UniversalPacketPlayOut{
	
    private String player;
    private String objectiveName;
    private int score;
    private Action action;
    
    public PacketPlayOutScoreboardScore(Action action, String objectiveName, String player, int score) {
        this.player = player;
        this.objectiveName = objectiveName;
        this.score = score;
        this.action = action;
    }
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet;
		if (versionNumber >= 13) {
			return newPacketPlayOutScoreboardScore_4.newInstance(action.toNMS(), objectiveName, player, score);
		} else {
			if (action == Action.REMOVE) {
				return newPacketPlayOutScoreboardScore_1.newInstance(player);
			} else {
				packet = newPacketPlayOutScoreboardScore_0.newInstance();
				PacketPlayOutScoreboardScore_PLAYER.set(packet, player);
				PacketPlayOutScoreboardScore_OBJECTIVENAME.set(packet, objectiveName);
				PacketPlayOutScoreboardScore_SCORE.set(packet, score);
				PacketPlayOutScoreboardScore_ACTION.set(packet, action.toNMS());
			}
		}
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new ScoreboardScore(player, action.toBungee(), objectiveName, score);
	}
	public enum Action{
		
        CHANGE((byte) 0, EnumConstant.PacketPlayOutScoreboardScore_Action_CHANGE),
        REMOVE((byte) 1, EnumConstant.PacketPlayOutScoreboardScore_Action_REMOVE);
        
		private byte bungeeEquivalent;
		private Object nmsEquivalent;
		
		private Action(byte bungeeEquivalent, Object nmsEquivalent) {
			this.bungeeEquivalent = bungeeEquivalent;
			this.nmsEquivalent = nmsEquivalent;
		}
	    public Object toNMS() {
	    	return nmsEquivalent;
	    }
	    public byte toBungee() {
	    	return bungeeEquivalent;
	    }
    }
	
	private static Class<?> PacketPlayOutScoreboardScore;
	private static Constructor<?> newPacketPlayOutScoreboardScore_0;
	private static Constructor<?> newPacketPlayOutScoreboardScore_1;
	private static Constructor<?> newPacketPlayOutScoreboardScore_4;
	private static Field PacketPlayOutScoreboardScore_PLAYER;
	private static Field PacketPlayOutScoreboardScore_OBJECTIVENAME;
	private static Field PacketPlayOutScoreboardScore_SCORE;
	private static Field PacketPlayOutScoreboardScore_ACTION;
	
	static {
		try {
			if (versionNumber >= 8) {
				PacketPlayOutScoreboardScore = getNMSClass("PacketPlayOutScoreboardScore");
				newPacketPlayOutScoreboardScore_0 = getConstructor(PacketPlayOutScoreboardScore, 0);
				(PacketPlayOutScoreboardScore_PLAYER = PacketPlayOutScoreboardScore.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutScoreboardScore_OBJECTIVENAME = PacketPlayOutScoreboardScore.getDeclaredField("b")).setAccessible(true);
				(PacketPlayOutScoreboardScore_SCORE = PacketPlayOutScoreboardScore.getDeclaredField("c")).setAccessible(true);
				(PacketPlayOutScoreboardScore_ACTION = PacketPlayOutScoreboardScore.getDeclaredField("d")).setAccessible(true);
				if (versionNumber >= 13) {
					newPacketPlayOutScoreboardScore_4 = getConstructor(PacketPlayOutScoreboardScore, 4);
				} else {
					newPacketPlayOutScoreboardScore_1 = PacketPlayOutScoreboardScore.getConstructor(String.class);
				}
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutScoreboardScore", e);
		}
	}
}