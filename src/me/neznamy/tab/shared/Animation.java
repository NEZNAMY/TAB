package me.neznamy.tab.shared;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.shared.placeholders.Constant;
import me.neznamy.tab.shared.placeholders.Placeholders;

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
		if (interval % 50 > 0) {
			int oldInterval = interval;
			interval -= interval%50;
			if (interval == 0) interval = 50;
			Shared.startupWarn("Animation \"&e" + name + "&c\" has a refresh interval of &e" + oldInterval + "ms&c which is not divisible by 50! Animations can't refresh faster than every tick ( = 50 milliseconds). &bUsing " + interval + ".");
		}
		if (list == null) {
			Shared.startupWarn("Animation \"&e" + name + "&c\" does not have any texts! &bIgnoring.");
			list = Arrays.asList("<Invalid Animation>");
		}
		this.name = name;
		this.messages = list.toArray(new String[0]);
		this.interval = interval;
		for (int i=0; i<messages.length; i++) {
			for (Constant c : Placeholders.myServerConstants.values()) {
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
}