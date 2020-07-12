package me.neznamy.tab.shared;

import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.Placeholders;

public interface MainClass {

	public void loadFeatures(boolean inject) throws Exception;
	public void sendConsoleMessage(String message);
	public void sendRawConsoleMessage(String message);
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) throws Exception;
	public void loadConfig() throws Exception;
	public void registerUnknownPlaceholder(String identifier);
	public void convertConfig(ConfigurationFile config);
	public String getServerVersion();
	public void suggestPlaceholders();
	
	public default void removeOld(ConfigurationFile config, String oldKey) {
		if (config.hasConfigOption(oldKey)) {
			config.set(oldKey, null);
			Shared.print('2', "Removed old " + config.getName() + " option " + oldKey);
		}
	}
	public default void rename(ConfigurationFile config, String oldName, String newName) {
		if (config.hasConfigOption(oldName)) {
			Object value = config.getObject(oldName);
			config.set(oldName, null);
			config.set(newName, value);
			Shared.print('2', "Renamed config option " + oldName + " to " + newName);
		}
	}
	public default void convert(ConfigurationFile config, String oldKey, Object oldValue, String newKey, Object newValue) {
		config.set(oldKey, null);
		config.set(newKey, newValue);
		Shared.print('2', "Converted old " + config.getName() + " option " + oldKey + " (" + oldValue + ") to new " + newKey + " (" + newValue + ")");
	}
	public default void suggestPlaceholderSwitch(String from, String to) {
		if (Placeholders.allUsedPlaceholderIdentifiers.contains(from)) {
			Shared.print('9', "Hint: Found used PlaceholderAPI placeholder \"&d" + from + "&9\". Consider replacing it with plugin's internal \"&d" + to + "&9\" for better performance.");
		}
	}
}