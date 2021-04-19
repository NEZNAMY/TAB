package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.event.CommandListener;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.rgb.TextColor;

/*
 * Sends plugin info to command sender
 */
public class PluginInfo implements CommandListener {
	
	@Override
	public boolean onCommand(TabPlayer sender, String message) {
		String command = TAB.getInstance().getPlatform().getSeparatorType().equals("server")? "/btab" : "/tab";
		if (message.equalsIgnoreCase(command) && (!TAB.getInstance().isPremium() || sender.hasPermission("tab.admin"))){
			IChatBaseComponent component = new IChatBaseComponent("TAB v" + TAB.getInstance().getPluginVersion()).setColor(new TextColor(EnumChatFormat.DARK_AQUA)).onHoverShowText('\u00a7' + "aClick to visit plugin's spigot page").onClickOpenUrl("https://www.spigotmc.org/resources/57806/");
			component.addExtra(new IChatBaseComponent(" by _NEZNAMY_").setColor(new TextColor(EnumChatFormat.BLACK)));
			sender.sendMessage(component);
		}
		return false;
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.OTHER;
	}
}