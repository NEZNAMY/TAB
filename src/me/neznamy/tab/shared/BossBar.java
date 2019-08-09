package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.PacketAPI.BossBAR;
import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarColor;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarStyle;

public class BossBar{

	public static boolean enable;
	public static List<BossBarLine> lines = new ArrayList<BossBarLine>();
	public static int refresh;
	
	public static void load() {
		if (!enable) return;
		for (BossBarLine l : lines) {
			for (ITabPlayer p : Shared.getPlayers()) sendBar(p, l);
		}
		Shared.scheduleRepeatingTask(refresh, "refreshing bossbar", Feature.BOSSBAR, new Runnable() {
			public void run() {
				for (BossBarLine line : lines) line.update();
			}
		});
	}
	public static boolean onChat(final ITabPlayer sender, String message) {
		if (!enable) return false;
		if (message.equalsIgnoreCase(Configs.bossbarToggleCommand)) {
			sender.bossbarVisible = !sender.bossbarVisible;
			if (sender.bossbarVisible) {
				for (BossBarLine line : lines) sendBar(sender, line);
				sender.sendMessage(Configs.bossbar_on);
			} else {
				for (BossBarLine line : lines) PacketAPI.removeBossBar(sender, line.getBossBar());
				sender.sendMessage(Configs.bossbar_off);
			}
			return true;
		}
		return false;
	}
	public static void playerJoin(ITabPlayer p) {
		if (enable) for (BossBarLine line : lines) sendBar(p, line);
	}
	public static void unload() {
		if (!enable) return;
		for (BossBarLine l : lines) {
			for (ITabPlayer p : Shared.getPlayers()) PacketAPI.removeBossBar(p, l.getBossBar());
		}
		lines.clear();
	}
	public static void sendBar(ITabPlayer p, BossBarLine l) {
		if (!p.bossbarVisible || p.disabledBossbar) return;
		String[] message_progress = Placeholders.replaceMultiple(p, l.getCurrentFrame().getMessage(), l.getCurrentFrame().getProgress());
		float progress;
		try {
			progress = Float.parseFloat(message_progress[1]);
		} catch(Exception e) {
			progress = 100;
			Shared.error("Invalid Bossbar progress: " + message_progress[1]);
		}
		PacketAPI.sendBossBar(p, l.getBossBar(), (float)progress/100, message_progress[0]);
	}
	public static class BossBarLine{
		
		private int refresh;
		private BossBarFrame[] frames;
		private BossBAR bossBar;
		
		public BossBarLine(int refresh, List<BossBarFrame> frames) {
			if (refresh == 0) {
				Shared.startupWarn("One of the BossBars has refresh interval of 0 milliseconds! Did you forget to configure it? Using 1000 to avoid issues.");
				refresh = 1000;
			}
			this.refresh = refresh;
			this.frames = frames.toArray(new BossBarFrame[0]);
			BossBarFrame f = frames.get(0);
			bossBar = new BossBAR(f.getStyle(), f.getColor());
		}
		public int getRefresh() {
			return refresh;
		}
		private BossBarFrame getCurrentFrame() {
			return frames[(int) ((System.currentTimeMillis()%(frames.length*refresh))/refresh)];
		}
		public BossBAR getBossBar() {
			return bossBar;
		}
		public void update() {
			BossBarFrame f = getCurrentFrame();
			for (ITabPlayer all : Shared.getPlayers()) {
				if (all.disabledBossbar) continue;
				String[] message_progress = Placeholders.replaceMultiple(all, f.getMessage(), f.getProgress());
				float progress;
				try {
					progress = Float.parseFloat(message_progress[1]);
				} catch(Exception e) {
					progress = 100;
					Shared.error("Invalid Bossbar progress: " + message_progress[1]);
				}
				PacketAPI.updateBossBar(all, bossBar, f.getColor(), f.getStyle(), (float)progress/100, message_progress[0]);
			}
		}
	}
	
	public static class BossBarFrame{
		
		private BarStyle style;
		private BarColor color;
		private String progress;
		private String message;
		
		public BossBarFrame(String style, String color, String progress, String message) {
			try {
				this.color = BarColor.valueOf(color);
			} catch (Exception e) {
				this.color = BarColor.PURPLE;
				Shared.startupWarn("\"" + color + "\" is not a valid boss bar color!");
			}
			try {
				this.style = BarStyle.valueOf(style);
			} catch (Exception e) {
				this.style = BarStyle.PROGRESS;
				Shared.startupWarn("\"" + style + "\" is not a valid boss bar style!");
			}
			this.progress = progress;
			this.message = message;
		}
		public BarStyle getStyle() {
			return style;
		}
		public BarColor getColor() {
			return color;
		}
		public String getProgress() {
			return progress;
		}
		public String getMessage() {
			return message;
		}
	}
}