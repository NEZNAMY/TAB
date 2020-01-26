package me.neznamy.tab.shared;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import me.neznamy.tab.platforms.bukkit.PerWorldPlayerlist;
import me.neznamy.tab.platforms.bukkit.PlaceholderAPIExpansion;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagX;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarColor;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarStyle;
import me.neznamy.tab.shared.packets.PacketPlayOutChat.ChatMessageType;
import me.neznamy.tab.shared.placeholders.*;

public class Shared {

	private static final String newline = System.getProperty("line.separator");
	public static final String DECODER_NAME = "TABReader";
	public static final ExecutorService exe = Executors.newCachedThreadPool();
	public static final String pluginVersion = "2.6.5-pre4";
	public static final int currentVersionId = 264;
	public static final DecimalFormat decimal2 = new DecimalFormat("#.##");
	public static final DecimalFormat decimal3 = new DecimalFormat("#.###");
	public static final char COLOR = '\u00a7';

	public static ConcurrentHashMap<UUID, ITabPlayer> data = new ConcurrentHashMap<UUID, ITabPlayer>();

	public static ConcurrentHashMap<Feature, Long> cpuLastSecond = new ConcurrentHashMap<Feature, Long>();
	public static List<CPUSample> cpuHistory = new ArrayList<CPUSample>();

	public static ConcurrentHashMap<String, Long> placeholderCpuLastSecond = new ConcurrentHashMap<String, Long>();
	public static List<ConcurrentHashMap<String, Long>> placeholderCpuHistory = new ArrayList<ConcurrentHashMap<String, Long>>();

	public static boolean disabled;
	private static List<Future<?>> tasks = new ArrayList<Future<?>>();
	public static int startupWarns = 0;
	public static MainClass mainClass;
	public static String separatorType;

	public static Collection<ITabPlayer> getPlayers(){
		return data.values();
	}
	public static ITabPlayer getPlayer(String name) {
		for (ITabPlayer p : data.values()) {
			if (p.getName().equals(name)) return p;
		}
		return null;
	}
	public static ITabPlayer getPlayer(int entityId) {
		for (ITabPlayer p : data.values()) {
			if (((TabPlayer)p).player.getEntityId() == entityId) return p;
		}
		return null;
	}
	public static ITabPlayer getPlayer(UUID uniqueId) {
		return data.get(uniqueId);
	}
	public static ITabPlayer getPlayerByTablistUUID(UUID tablistId) {
		for (ITabPlayer p : data.values()) {
			if (p.getTablistId().toString().equals(tablistId.toString())) {
				return p;
			}
		}
		return null;
	}
	public static <T> T error(T defaultValue, String message) {
		return error(defaultValue, message, null);
	}
	public static <T> T error(T defaultValue, String message, Throwable t) {
		try {
			if (!Configs.errorFile.exists()) Configs.errorFile.createNewFile();
			if (Configs.errorFile.length() < 1000000) { //not going over 1 MB
				BufferedWriter buf = new BufferedWriter(new FileWriter(Configs.errorFile, true));
				if (message != null) {
					buf.write(ERROR_PREFIX() + "[TAB v" + pluginVersion + "] " + message + newline);
					if (Configs.SECRET_log_errors_into_console) print('c', message);
				}
				if (t != null) {
					buf.write(ERROR_PREFIX() + t.getClass().getName() +": " + t.getMessage() + newline);
					if (Configs.SECRET_log_errors_into_console) printClean("&c" + t.getClass().getName() +": " + t.getMessage());
					for (StackTraceElement ste : t.getStackTrace()) {
						buf.write(ERROR_PREFIX() + "       at " + ste.toString() + newline);
						if (Configs.SECRET_log_errors_into_console) printClean("&c       at " + ste.toString());
					}
				}
				buf.close();
			}
		} catch (Throwable ex) {
			print('c', "An error occurred when printing error message into file");
			ex.printStackTrace();
			print('c', "Original error: " + message);
			if (t != null) t.printStackTrace();
		}
		return defaultValue;
	}
	public static void startCPUTask() {
		scheduleRepeatingTask(1000, "calculating cpu usage", Feature.OTHER, new Runnable() {

			public void run() {
				cpuHistory.add(new CPUSample(cpuLastSecond));
				cpuLastSecond = new ConcurrentHashMap<Feature, Long>();
				if (cpuHistory.size() > 60) cpuHistory.remove(0);

				placeholderCpuHistory.add(placeholderCpuLastSecond);
				placeholderCpuLastSecond = new ConcurrentHashMap<String, Long>();
				if (placeholderCpuHistory.size() > 60) placeholderCpuHistory.remove(0);
			}
		});
	}
	private static String ERROR_PREFIX() {
		return new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ").format(new Date());
	}
	public static void startupWarn(String message) {
		print('c', message);
		startupWarns++;
	}
	public static void print(char color, String message) {
		mainClass.sendConsoleMessage("&" + color + "[TAB] " + message);
	}
	public static void printClean(String message) {
		mainClass.sendConsoleMessage(message);
	}
	public static void featureCPU(Feature feature, long value) {
		Long previous = cpuLastSecond.get(feature);
		if (previous != null) {
			cpuLastSecond.put(feature, previous+value);
		} else {
			cpuLastSecond.put(feature, value);
		}
	}
	public static void placeholderCpu(String placeholder, long value) {
		Long previous = placeholderCpuLastSecond.get(placeholder);
		if (previous != null) {
			placeholderCpuLastSecond.put(placeholder, previous+value);
		} else {
			placeholderCpuLastSecond.put(placeholder, value);
		}
	}
	public static void scheduleRepeatingTask(int delayMilliseconds, String description, Feature feature, Runnable r) {
		if (delayMilliseconds <= 0) return;
		tasks.add(exe.submit(new Runnable() {

			public void run() {
				while (true) {
					try {
						long time = System.nanoTime();
						r.run();
						featureCPU(feature, System.nanoTime()-time);
						Thread.sleep(delayMilliseconds);
					} catch (InterruptedException pluginDisabled) {
						break;
					} catch (Throwable t) {
						error(null, "An error occurred when " + description, t);
					}
				}
			}
		}));
	}
	public static void runTask(String description, Feature feature, Runnable r) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					long time = System.nanoTime();
					r.run();
					featureCPU(feature, System.nanoTime()-time);
				} catch (Throwable t) {
					error(null, "An error occurred when " + description, t);
				}
			}
		});
	}
	public static void cancelAllTasks() {
		for (Future<?> f : tasks) f.cancel(true);
	}
	public static void sendPluginInfo(ITabPlayer to) {
		IChatBaseComponent message = new IChatBaseComponent("TAB v" + pluginVersion).setColor(EnumChatFormat.DARK_AQUA).onHoverShowText(COLOR + "aClick to visit plugin's spigot page").onClickOpenUrl("https://www.spigotmc.org/resources/57806/");
		message.addExtra(new IChatBaseComponent(" by _NEZNAMY_ (discord: NEZNAMY#4659)").setColor(EnumChatFormat.BLACK));
		to.sendCustomPacket(new PacketPlayOutChat(message.toString(), ChatMessageType.CHAT));
	}
	public static void unload() {
		try {
			if (disabled) return;
			long time = System.currentTimeMillis();
			cancelAllTasks();
			Configs.animations = new ArrayList<Animation>();
			cpuHistory = new ArrayList<CPUSample>();
			cpuLastSecond = new ConcurrentHashMap<Feature, Long>();
			placeholderCpuHistory = new ArrayList<ConcurrentHashMap<String, Long>>();
			placeholderCpuLastSecond = new ConcurrentHashMap<String, Long>();
			HeaderFooter.unload();
			TabObjective.unload();
			BelowName.unload();
			Playerlist.unload();
			NameTag16.unload();
			BossBar.unload();
			ScoreboardManager.unload();
			if (separatorType.equals("world")) {
				NameTagX.unload();
				PerWorldPlayerlist.unload();
				if (PluginHooks.placeholderAPI) PlaceholderAPIExpansion.unregister();
			}
			data.clear();
			print('a', "Disabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Throwable e) {
			error(null, "Failed to unload the plugin", e);
		}
	}
	public static int parseInteger(String string, int defaultValue, String place) {
		try {
			return Integer.parseInt(string);
		} catch (Throwable e) {
			if (string.contains("%")) {
				return Shared.error(defaultValue, "Value \"" + string + "\" used in " + place + " still has unparsed placeholders! Did you forget to download an expansion ?");
			} else {
				return Shared.error(defaultValue, place + " only accepts numeric values! (Attempted to use \"" + string + "\")");
			}
		}
	}
	public static float parseFloat(String string, float defaultValue, String place) {
		try {
			return Float.parseFloat(string);
		} catch (Throwable e) {
			if (string.contains("%")) {
				return Shared.error(defaultValue, "Value \"" + string + "\" used in " + place + " still has unparsed placeholders! Did you forget to download an expansion ?");
			} else {
				return Shared.error(defaultValue, place + " only accepts numeric values! (Attempted to use \"" + string + "\")");
			}
		}
	}
	public static BarColor parseColor(String string, BarColor defaultValue, String place) {
		try {
			return BarColor.valueOf(string);
		} catch (Throwable e) {
			if (string.contains("%")) {
				return Shared.error(defaultValue, "Value \"" + string + "\" used in " + place + " still has unparsed placeholders! Did you forget to download an expansion ?");
			} else {
				return Shared.error(defaultValue, place + " only accepts one of the defined colors! (Attempted to use \"" + string + "\")");
			}
		}
	}
	public static BarStyle parseStyle(String string, BarStyle defaultValue, String place) {
		try {
			return BarStyle.valueOf(string);
		} catch (Throwable e) {
			if (string.contains("%")) {
				return Shared.error(defaultValue, "Value \"" + string + "\" used in " + place + " still has unparsed placeholders! Did you forget to download an expansion ?");
			} else {
				return Shared.error(defaultValue, place + " only accepts one of the defined styles! (Attempted to use \"" + string + "\")");
			}
		}
	}
	public static void checkForUpdates() {
		exe.submit(new Runnable() {

			@Override
			public void run() {
				try {
					HttpURLConnection con = (HttpURLConnection) new URL("http://207.180.242.97/spigot/tab/latest.version").openConnection();
					BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String versionId = br.readLine();
					String versionString = br.readLine();
					br.close();
					int latestVersion = Integer.parseInt(versionId);
					if (latestVersion > currentVersionId) {
						Shared.print('b', "Version " + versionString + " is out! Your version: " + pluginVersion);
						Shared.print('b', "Get the update at https://www.spigotmc.org/resources/57806/");
					}
				} catch (Exception e) {
//					Shared.print('c', "Failed to check for updates (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
				}
			}
		});
	}
	
	public static void registerAnimationPlaceholders() {
		for (Animation a : Configs.animations) {
			Placeholders.serverPlaceholders.add(new ServerPlaceholder("%animation:" + a.getName() + "%", 0) {
				public String get() {
					return a.getMessage();
				}
				@Override
				public String[] getChilds(){
					return a.getAllMessages();
				}
			});
			Placeholders.serverPlaceholders.add(new ServerPlaceholder("{animation:" + a.getName() + "}", 0) {
				public String get() {
					return a.getMessage();
				}
				@Override
				public String[] getChilds(){
					return a.getAllMessages();
				}
			});
		}
	}
	public static void registerUniversalPlaceholders() {
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%rank%", 1000) {
			public String get(ITabPlayer p) {
				return p.getRank();
			}
			@Override
			public String[] getChilds(){
				return Configs.rankAliases.values().toArray(new String[0]);
			}
		});
		for (Entry<String, Integer> entry : Placeholders.online.entrySet()){
			Placeholders.serverPlaceholders.add(new ServerPlaceholder("%version-group:" + entry.getKey()+ "%", 5000) {
				public String get() {
					return Placeholders.online.get(entry.getKey())+"";
				}
			});
		}
		Placeholders.serverPlaceholders.add(new ServerPlaceholder("%staffonline%", 2000) {
			public String get() {
				int var = 0;
				for (ITabPlayer all : getPlayers()){
					if (all.isStaff()) var++;
				}
				return var+"";
			}
		});
		Placeholders.serverPlaceholders.add(new ServerPlaceholder("%nonstaffonline%", 2000) {
			public String get() {
				int var = getPlayers().size();
				for (ITabPlayer all : getPlayers()){
					if (all.isStaff()) var--;
				}
				return var+"";
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%"+separatorType+"%", 1000) {
			public String get(ITabPlayer p) {
				if (Configs.serverAliases != null && Configs.serverAliases.containsKey(p.getWorldName())) return Configs.serverAliases.get(p.getWorldName())+""; //bungee only
				return p.getWorldName();
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%"+separatorType+"online%", 1000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) var++;
				}
				return var+"";
			}
		});
		Placeholders.serverPlaceholders.add(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "");
			}
		});
		Placeholders.constants.add(new Constant("%memory-max%") {
			public String get() {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"";
			}
		});
		Placeholders.serverPlaceholders.add(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return (decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + "");
			}
		});
		Placeholders.constants.add(new Constant("%memory-max-gb%") {
			public String get() {
				return (decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"";
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%nick%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getName();
			}
		});
		Placeholders.serverPlaceholders.add(new ServerPlaceholder("%time%", 900) {
			public String get() {
				return Configs.timeFormat.format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		Placeholders.serverPlaceholders.add(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return Configs.dateFormat.format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		Placeholders.serverPlaceholders.add(new ServerPlaceholder("%online%", 1000) {
			public String get() {
				return getPlayers().size()+"";
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%ping%", 2000) {
			public String get(ITabPlayer p) {
				return p.getPing()+"";
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%player-version%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
	}
	public static enum Feature{

		NAMETAG("Name tags"),
		NAMETAGAO("Name tag anti-override"),
		PLAYERLIST_1("Tablist names 1"),
		PLAYERLIST_2("Tablist names 2"),
		BOSSBAR("Boss Bar"),
		SCOREBOARD("Scoreboard"),
		HEADERFOOTER("Header/Footer"),
		TABLISTOBJECTIVE("Tablist objective"),
		NAMETAGX("Unlimited nametag mode"),
		BELOWNAME("Belowname"),
		OTHER("Other");

		private String string;

		Feature(String string) {
			this.string = string;
		}
		public String toString() {
			return string;
		}
	}
	public static class CPUSample{

		private ConcurrentHashMap<Feature, Long> values;

		public CPUSample(ConcurrentHashMap<Feature, Long> cpuLastSecond) {
			this.values = cpuLastSecond;
		}
		public long getTotalCpuTime() {
			long time = 0;
			for (long value : values.values()) {
				time += value;
			}
			return time;
		}
		public ConcurrentHashMap<Feature, Long> getValues(){
			return values;
		}
	}
}