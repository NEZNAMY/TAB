package me.neznamy.tab.shared;

import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public interface MainClass {

	public void sendConsoleMessage(String message);
	public String getPermissionPlugin();
	public String getSeparatorType();
	public void reload(ITabPlayer sender);
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) throws Exception;
	public void loadConfig() throws Exception;
}