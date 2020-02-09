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

import me.neznamy.tab.api.TABAPI;
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
	public static final String CHANNEL_NAME = "tab:placeholders";
	public static final ExecutorService exe = Executors.newCachedThreadPool();
	public static final String pluginVersion = "2.7.0-pre1";
	public static final int currentVersionId = 265;
	public static final DecimalFormat decimal2 = new DecimalFormat("#.##");
	public static final DecimalFormat decimal3 = new DecimalFormat("#.###");
	public static final char COLOR = '\u00a7';

	public static ConcurrentHashMap<UUID, ITabPlayer> data = new ConcurrentHashMap<UUID, ITabPlayer>();

	public static boolean disabled;
	private static List<Future<?>> tasks = new ArrayList<Future<?>>();
	public static int startupWarns = 0;
	public static MainClass mainClass;
	public static String separatorType;
	public static CPUManager cpu;

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
					if (Configs.SECRET_debugMode) print('c', message);
				}
				if (t != null) {
					buf.write(ERROR_PREFIX() + t.getClass().getName() +": " + t.getMessage() + newline);
					if (Configs.SECRET_debugMode) printClean("&c" + t.getClass().getName() +": " + t.getMessage());
					for (StackTraceElement ste : t.getStackTrace()) {
						buf.write(ERROR_PREFIX() + "       at " + ste.toString() + newline);
						if (Configs.SECRET_debugMode) printClean("&c       at " + ste.toString());
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
	public static void debug(String message) {
		if (Configs.SECRET_debugMode) mainClass.sendConsoleMessage("&" + 7 + "[TAB DEBUG] " + message);
	}
	
	public static void scheduleRepeatingTask(int delayMilliseconds, String description, String feature, Runnable r) {
		if (delayMilliseconds <= 0) return;
		debug("Starting repeating task [" + feature + "] with refresh " + delayMilliseconds + "ms");
		tasks.add(exe.submit(new Runnable() {

			public void run() {
				while (true) {
					try {
						long time = System.nanoTime();
						r.run();
						cpu.addFeatureTime(feature.toString(), System.nanoTime()-time);
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
	public static void runTask(String description, String feature, Runnable r) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					long time = System.nanoTime();
					r.run();
					cpu.addFeatureTime(feature.toString(), System.nanoTime()-time);
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
	public static void registerUniversalPlaceholders() {
		for (Animation a : Configs.animations) {
			TABAPI.registerServerPlaceholder(new ServerPlaceholder("%animation:" + a.getName() + "%", a.getInterval()-1) {
				public String get() {
					return a.getMessage();
				}
				@Override
				public String[] getChilds(){
					return a.getAllMessages();
				}
			});
			TABAPI.registerServerPlaceholder(new ServerPlaceholder("{animation:" + a.getName() + "}", a.getInterval()-1) {
				public String get() {
					return a.getMessage();
				}
				@Override
				public String[] getChilds(){
					return a.getAllMessages();
				}
			});
		}
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%rank%", 1000) {
			public String get(ITabPlayer p) {
				return p.getRank();
			}
			@Override
			public String[] getChilds(){
				return Configs.rankAliases.values().toArray(new String[0]);
			}
		});
		for (Entry<String, Integer> entry : Placeholders.online.entrySet()){
			TABAPI.registerServerPlaceholder(new ServerPlaceholder("%version-group:" + entry.getKey()+ "%", 5000) {
				public String get() {
					return Placeholders.online.get(entry.getKey())+"";
				}
			});
		}
		TABAPI.registerServerPlaceholder(new ServerPlaceholder("%staffonline%", 2000) {
			public String get() {
				int var = 0;
				for (ITabPlayer all : getPlayers()){
					if (all.isStaff()) var++;
				}
				return var+"";
			}
		});
		TABAPI.registerServerPlaceholder(new ServerPlaceholder("%nonstaffonline%", 2000) {
			public String get() {
				int var = getPlayers().size();
				for (ITabPlayer all : getPlayers()){
					if (all.isStaff()) var--;
				}
				return var+"";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%"+separatorType+"%", 1000) {
			public String get(ITabPlayer p) {
				if (Configs.serverAliases != null && Configs.serverAliases.containsKey(p.getWorldName())) return Configs.serverAliases.get(p.getWorldName())+""; //bungee only
				return p.getWorldName();
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%"+separatorType+"online%", 1000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) var++;
				}
				return var+"";
			}
		});
		TABAPI.registerServerPlaceholder(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "");
			}
		});
		TABAPI.registerServerConstant(new Constant("%memory-max%") {
			public String get() {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"";
			}
		});
		TABAPI.registerServerPlaceholder(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return (decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + "");
			}
		});
		TABAPI.registerServerConstant(new Constant("%memory-max-gb%") {
			public String get() {
				return (decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%nick%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getName();
			}
		});
		TABAPI.registerServerPlaceholder(new ServerPlaceholder("%time%", 900) {
			public String get() {
				return Configs.timeFormat.format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		TABAPI.registerServerPlaceholder(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return Configs.dateFormat.format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		TABAPI.registerServerPlaceholder(new ServerPlaceholder("%online%", 1000) {
			public String get() {
				return getPlayers().size()+"";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%ping%", 2000) {
			public String get(ITabPlayer p) {
				return p.getPing()+"";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%player-version%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
		for (String placeholder : Placeholders.usedPlaceholders) {
			if (!Placeholders.usedServerPlaceholders.containsKey(placeholder) && 
				!Placeholders.usedPlayerPlaceholders.containsKey(placeholder) && 
				!Placeholders.usedServerConstants.containsKey(placeholder)) {
				Configs.assignPlaceholder(placeholder);
			}
		}
	}
}