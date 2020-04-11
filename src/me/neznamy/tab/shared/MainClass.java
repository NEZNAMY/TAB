package me.neznamy.tab.shared;

import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public interface MainClass {

	public void loadFeatures(boolean inject) throws Exception;
	public void sendConsoleMessage(String message);
	public void sendRawConsoleMessage(String message);
	public String getPermissionPlugin();
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) throws Exception;
	public void loadConfig() throws Exception;
	public void registerUnknownPlaceholder(String identifier);
	public void convertConfig(ConfigurationFile config);
	
	public default void ticks2Millis(ConfigurationFile config, String oldKey, String newKey) {
		if (config.hasConfigOption(oldKey)) {
			convert(config, oldKey, config.getObject(oldKey), newKey, config.getInt(oldKey) * 50);
		}
	}
	public default void removeOld(ConfigurationFile config, String oldKey) {
		if (config.hasConfigOption(oldKey)) {
			config.set(oldKey, null);
			Shared.print('2', "Removed old " + config.getName() + " option " + oldKey);
		}
	}
	public default void rename(ConfigurationFile config, String oldName, String newName) {
		if (config.hasConfigOption(oldName)) {
			convert(config, oldName, config.getObject(oldName), newName, config.getObject(oldName));
		}
	}
	public default void convert(ConfigurationFile config, String oldKey, Object oldValue, String newKey, Object newValue) {
		config.set(oldKey, null);
		config.set(newKey, newValue);
		Shared.print('2', "Converted old " + config.getName() + " option " + oldKey + " (" + oldValue + ") to new " + newKey + " (" + newValue + ")");
	}
}