package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
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
		String command = TAB.getInstance().getPlatform().getSeparatorType().equals("server")? "/btab" : "/tab";
		if (message.equalsIgnoreCase(command) && sender.hasPermission("tab.admin")){
			IChatBaseComponent component = new IChatBaseComponent("\u00a73TAB v" + TAB.PLUGIN_VERSION);
			component.getModifier().onHoverShowText(new IChatBaseComponent("\u00a7aClick to visit plugin's spigot page"));
			component.getModifier().onClickOpenUrl("https://github.com/NEZNAMY/TAB");
			component.addExtra(new IChatBaseComponent("\u00a70 by _NEZNAMY_"));
			sender.sendMessage(component);
		}
		return false;
	}
}