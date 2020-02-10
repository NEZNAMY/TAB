package me.neznamy.tab.shared;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.ServerConstant;

public class Animation {
	
	private String name;
	private String[] messages;
	private int interval;
	
	public Animation(String name, List<String> list, int interval){
		if (interval == 0) {
			Shared.startupWarn("Animation \"&e" + name + "&c\" has refresh interval of 0 milliseconds! Did you forget to configure it? &bUsing 1000.");
			interval = 1000;
		}
		if (interval < 0) {
			Shared.startupWarn("Animation \"&e" + name + "&c\" has refresh interval of "+interval+". Refresh cannot be negative! &bUsing 1000.");
			interval = 1000;
		}
		if (list == null) {
			Shared.startupWarn("Animation \"&e" + name + "&c\" does not have any texts! &bIgnoring.");
			list = Arrays.asList("<Invalid Animation>");
		}
		this.name = name;
		this.messages = list.toArray(new String[0]);
		this.interval = interval;
		for (int i=0; i<messages.length; i++) {
			for (ServerConstant c : Placeholders.myServerConstants.values()) {
				if (messages[i].contains(c.getIdentifier())) {
					messages[i] = messages[i].replace(c.getIdentifier(), c.get());
				}
			}
		}
	}
	public String[] getAllMessages() {
		return messages;
	}
	public String getMessage(){
		return messages[(int) ((System.currentTimeMillis()%(messages.length*interval))/interval)];
	}
	public String getName(){
		return name;
	}
	public int getInterval() {
		return interval;
	}
}