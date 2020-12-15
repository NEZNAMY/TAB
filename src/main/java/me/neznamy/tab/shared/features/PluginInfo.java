package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.rgb.TextColor;

/*
 * Sends plugin info to command sender
 */
public class PluginInfo implements CommandListener {

	@Override
	public boolean onCommand(TabPlayer sender, String message) {
		String command = Shared.platform.getSeparatorType().equals("server")? "/btab" : "/tab";
		if (message.equalsIgnoreCase(command) && (!Premium.is() || sender.hasPermission("tab.admin"))){
			IChatBaseComponent component = new IChatBaseComponent("TAB v" + Shared.pluginVersion).setColor(TextColor.of(EnumChatFormat.DARK_AQUA)).onHoverShowText(PlaceholderManager.colorChar + "aClick to visit plugin's spigot page").onClickOpenUrl("https://www.spigotmc.org/resources/57806/");
			component.addExtra(new IChatBaseComponent(" by _NEZNAMY_").setColor(TextColor.of(EnumChatFormat.BLACK)));
			sender.sendCustomPacket(new PacketPlayOutChat(component));
		}
		return false;
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.OTHER;
	}
}