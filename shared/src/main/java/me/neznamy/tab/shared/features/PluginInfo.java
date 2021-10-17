package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;

/*
 * Sends plugin info to command sender
 */
public class PluginInfo extends TabFeature {
	
	public PluginInfo() {
		super("Plugin info");
	}

	@Override
	public boolean onCommand(TabPlayer sender, String message) {
		String command = TAB.getInstance().getPlatform().isProxy() ? "/btab" : "/tab";
		if (message.equalsIgnoreCase(command) && sender.hasPermission("tab.admin")){
			IChatBaseComponent component = new IChatBaseComponent(EnumChatFormat.color("&3TAB v") + TAB.PLUGIN_VERSION);
			component.getModifier().onHoverShowText(new IChatBaseComponent(EnumChatFormat.color("&aClick to visit plugin's spigot page")));
			component.getModifier().onClickOpenUrl("https://github.com/NEZNAMY/TAB");
			component.addExtra(new IChatBaseComponent(EnumChatFormat.color("&0 by _NEZNAMY_")));
			sender.sendMessage(component);
		}
		return false;
	}
}