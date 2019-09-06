package me.neznamy.tab.shared;

import java.io.BufferedWriter;
import java.io.FileWriter;
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

import me.neznamy.tab.shared.FancyMessage.ClickAction;
import me.neznamy.tab.shared.FancyMessage.Extra;
import me.neznamy.tab.shared.FancyMessage.HoverAction;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;

public class Shared {

	private static final String newline = System.getProperty("line.separator");
	public static final String DECODER_NAME = "TABReader";
	public static final ExecutorService exe = Executors.newCachedThreadPool();
	public static final boolean consoleErrors = true;

	public static ConcurrentHashMap<UUID, ITabPlayer> data = new ConcurrentHashMap<UUID, ITabPlayer>();
	public static ConcurrentHashMap<Feature, Long> cpuLastSecond = new ConcurrentHashMap<Feature, Long>();
	public static List<CPUSample> cpuHistory = new ArrayList<CPUSample>();
	private static int nextEntityId = 2000000000;
	private static List<Future<?>> tasks = new ArrayList<Future<?>>();
	public static String pluginVersion;
	public static int startupWarns = 0;
	public static MainClass mainClass;

	public static void init(MainClass mainClass, String pluginVersion) {
		Shared.mainClass = mainClass;
		Shared.pluginVersion = pluginVersion;
	}
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
			if (p.getEntityId() == entityId) return p;
		}
		return null;
	}
	public static ITabPlayer getPlayer(UUID uniqueId) {
		return data.get(uniqueId);
	}
	public static void error(String message) {
		error(message, null);
	}
	public static void error(String message, Throwable t) {
		try {
			if (!Configs.errorFile.exists()) Configs.errorFile.createNewFile();
			BufferedWriter buf = new BufferedWriter(new FileWriter(Configs.errorFile, true));
			if (message != null) {
				buf.write(ERROR_PREFIX() + "[TAB v" + pluginVersion + "] " + message + newline);
				if (consoleErrors) print("§c", message);
			}
			if (t != null) {
				buf.write(ERROR_PREFIX() + t.getClass().getName() +": " + t.getMessage() + newline);
				if (consoleErrors) printClean("§c" + t.getClass().getName() +": " + t.getMessage());
				for (StackTraceElement ste : t.getStackTrace()) {
					buf.write(ERROR_PREFIX() + "       at " + ste.toString() + newline);
					if (consoleErrors) printClean("§c       at " + ste.toString());
				}
			}
			buf.close();
		} catch (Throwable ex) {
			print("§c", "An error occured when generating error message");
			ex.printStackTrace();
			print("§c", "Original error: " + message);
			if (t != null) t.printStackTrace();
		}
	}
	public static String round(double value) {
		return new DecimalFormat("#.##").format(value);
	}
	public static void startCPUTask() {
		scheduleRepeatingTask(1000, "calculating cpu usage", Feature.OTHER, new Runnable() {

			public void run() {
				cpuHistory.add(new CPUSample(cpuLastSecond));
				cpuLastSecond = new ConcurrentHashMap<Feature, Long>();
				if (cpuHistory.size() > 60*15) cpuHistory.remove(0); //15 minute history
			}
		});
	}
	private static String ERROR_PREFIX() {
		return new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ").format(new Date());
	}
	public static int getNextEntityId() {
		return nextEntityId++;
	}
	public static void startupWarn(String message) {
		print("§c", message);
		startupWarns++;
	}
	public static void print(String color, String message) {
		mainClass.sendConsoleMessage(color + "[TAB] " + message);
	}
	public static void printClean(String message) {
		mainClass.sendConsoleMessage(message);
	}
	public static void cpu(Feature feature, long value) {
		Long previous = cpuLastSecond.get(feature);
		if (previous != null) {
			cpuLastSecond.put(feature, previous+value);
		} else {
			cpuLastSecond.put(feature, value);
		}
	}
	public static void scheduleRepeatingTask(final int delayMilliseconds, final String description, final Feature feature, final Runnable r) {
		if (delayMilliseconds <= 0) return;
		tasks.add(exe.submit(new Runnable() {

			public void run() {
				while (true) {
					try {
						long time = System.nanoTime();
						r.run();
						cpu(feature, System.nanoTime()-time);
						Thread.sleep(delayMilliseconds);
					} catch (InterruptedException e) {
						break;
					} catch (Throwable t) {
						error("An error occured when " + description, t);
					}
				}
			}
		}));
	}
	public static void runTask(final String description, final Feature feature, final Runnable r) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					long time = System.nanoTime();
					r.run();
					cpu(feature, System.nanoTime()-time);
				} catch (Throwable t) {
					error("An error occured when " + description, t);
				}
			}
		});
	}
	public static void runTaskLater(final int delayMilliseconds, final String description, final Feature feature, final Runnable r) {
		final Future<?>[] array = new Future[1];
		array[0] = exe.submit(new Runnable() {


			public void run() {
				try {
					Thread.sleep(delayMilliseconds);
					long time = System.nanoTime();
					r.run();
					cpu(feature, System.nanoTime()-time);
				} catch (InterruptedException e) {
				} catch (Throwable e) {
					error("An error occured when " + description, e);
				}
				tasks.remove(array[0]);
			}
		});
		tasks.add(array[0]);
	}
	public static void cancelAllTasks() {
		for (Future<?> f : tasks) f.cancel(true);
	}
	public static void sendPluginInfo(ITabPlayer to) {
		FancyMessage message = new FancyMessage();
		message.add(new Extra("§3TAB v" + Shared.pluginVersion).onHover(HoverAction.SHOW_TEXT, "§aClick to visit plugin's spigot page").onClick(ClickAction.OPEN_URL, "https://www.spigotmc.org/resources/57806/"));
		message.add(new Extra(" §0by _NEZNAMY_ (discord: NEZNAMY#4659)"));
		new PacketPlayOutChat(message.toString()).send(to);
	}
	public static void registerUniversalPlaceholders() {
		Placeholders.list.add(new Placeholder("%money%") {
			public String get(ITabPlayer p) {
				return p.getMoney();
			}
		});
		Placeholders.list.add(new Placeholder("%rank%") {
			public String get(ITabPlayer p) {
				return p.getRank();
			}
			@Override
			public String[] getChilds(){
				return Configs.rankAliases.values().toArray(new String[0]);
			}
		});
		for (final Entry<String, Integer> entry : Placeholders.online.entrySet()){
			Placeholders.list.add(new Placeholder("%version-group:" + entry.getKey()+ "%") {
				public String get(ITabPlayer p) {
					return Placeholders.online.get(entry.getKey())+"";
				}
			});
		}
		for (final Animation a : Configs.animations) {
			Placeholders.list.add(new Placeholder("%animation:" + a.getName() + "%") {
				public String get(ITabPlayer p) {
					return a.getMessage();
				}
				@Override
				public String[] getChilds(){
					return a.getAllMessages();
				}
			});
			Placeholders.list.add(new Placeholder("{animation:" + a.getName() + "}") {
				public String get(ITabPlayer p) {
					return a.getMessage();
				}
				@Override
				public String[] getChilds(){
					return a.getAllMessages();
				}
			});
		}
		Placeholders.list.add(new Placeholder("%staffonline%") {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var++;
				}
				return var+"";
			}
		});
		Placeholders.list.add(new Placeholder("%"+Shared.mainClass.getSeparatorType()+"%") {
			public String get(ITabPlayer p) {
				return p.getWorldName();
			}
		});
		Placeholders.list.add(new Placeholder("%"+Shared.mainClass.getSeparatorType()+"online%") {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) var++;
				}
				return var+"";
			}
		});
		Placeholders.list.add(new Placeholder("%memory-used%") {
			public String get(ITabPlayer p) {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "");
			}
		});
		Placeholders.list.add(new Placeholder("%memory-max%") {
			public String get(ITabPlayer p) {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"";
			}
		});
		Placeholders.list.add(new Placeholder("%memory-used-gb%") {
			public String get(ITabPlayer p) {
				return (Shared.round((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + "");
			}
		});
		Placeholders.list.add(new Placeholder("%memory-max-gb%") {
			public String get(ITabPlayer p) {
				return (Shared.round((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"";
			}
		});
		Placeholders.list.add(new Placeholder("%nick%") {
			public String get(ITabPlayer p) {
				return p.getName();
			}
		});
		Placeholders.list.add(new Placeholder("%time%") {
			public String get(ITabPlayer p) {
				return new SimpleDateFormat(Configs.timeFormat).format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		Placeholders.list.add(new Placeholder("%date%") {
			public String get(ITabPlayer p) {
				return new SimpleDateFormat(Configs.dateFormat).format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		Placeholders.list.add(new Placeholder("%online%") {
			public String get(ITabPlayer p) {
				return Shared.getPlayers().size()+"";
			}
		});
		Placeholders.list.add(new Placeholder("%ping%") {
			public String get(ITabPlayer p) {
				return p.getPing()+"";
			}
		});
		Placeholders.list.add(new Placeholder("%player-version%") {
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