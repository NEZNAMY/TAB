package me.neznamy.tab.shared;

import java.util.Map;

import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public interface MainClass {

	public void sendConsoleMessage(String message);
	public String getPermissionPlugin();
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) throws Exception;
	public void loadConfig() throws Exception;
	public void load(boolean broadcastTime, boolean inject);
	public void registerUnknownPlaceholder(String identifier);
	public boolean convertConfig(Map<String, Object> values);
}