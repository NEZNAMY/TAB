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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Shared {

	public static ServerType servertype;
	public static ConcurrentHashMap<UUID, ITabPlayer> data = new ConcurrentHashMap<UUID, ITabPlayer>();
	public static long cpuTime;
	private static int nextEntityId = 2000000000;
	public static List<Long> cpuValues = new ArrayList<Long>();
	private static List<Future<?>> tasks = new ArrayList<Future<?>>();
	private static ExecutorService exe = Executors.newCachedThreadPool();
	public static String pluginVersion;
	private static final String newline = System.getProperty("line.separator");
	public static int startupWarns = 0;
	public static MainClass mainClass;
	public static final String DECODER_NAME = "TABReader";

	public static void init(MainClass mainClass, ServerType serverType, String pluginVersion) {
		Shared.mainClass = mainClass;
		servertype = serverType;
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
		error(message, null, null);
	}
	public static void error(String message, Exception e) {
		error(message, e, null);
	}
	public static void error(String message, Error err) {
		error(message, null, err);
	}
	public static void error(String message, Exception e, Error err) {
		try {
			if (!Configs.errorFile.exists()) Configs.errorFile.createNewFile();
			BufferedWriter buf = new BufferedWriter(new FileWriter(Configs.errorFile, true));
			if (message != null) {
				buf.write(ERROR_PREFIX() + "[TAB v" + pluginVersion + "] " + message + newline);
//				print("§c", message);
			}
			if (e != null) {
				buf.write(ERROR_PREFIX() + e.getClass().getName() +": " + e.getMessage() + newline);
//				printClean("§c" + e.getClass().getName() +": " + e.getMessage());
				for (StackTraceElement ste : e.getStackTrace()) {
					buf.write(ERROR_PREFIX() + "       at " + ste.toString() + newline);
//					printClean("§c       at " + ste.toString());
				}
			}
			if (err != null) {
				buf.write(ERROR_PREFIX() + err.getClass().getName() +": " + err.getMessage() + newline);
//				printClean("§c" + e.getClass().getName() +": " + e.getMessage());
				for (StackTraceElement ste : err.getStackTrace()) {
					buf.write(ERROR_PREFIX() + "       at " + ste.toString() + newline);
//					printClean("§c       at " + ste.toString());
				}
			}
			buf.close();
		} catch (Exception ex) {
			print("§c", "An error occured when generating error message");
			ex.printStackTrace();
			print("§c", "Original error: " + message);
			if (e != null) e.printStackTrace();
			if (err != null) err.printStackTrace();
		}
	}
	public static String round(double value) {
		return new DecimalFormat("#.##").format(value);
	}
	public static void startCPUTask() {
		scheduleRepeatingTask(1000, "calculating cpu usage", new Runnable() {
			
			public void run() {
				cpuValues.add(cpuTime);
//				for (ITabPlayer p : getPlayers()) p.sendMessage(round((float)cpuTime/10000000) + "%"); 
				cpuTime = 0;
				if (cpuValues.size() > 60*15) cpuValues.remove(0); //15 minute history
			}
		});
	}
	private static String ERROR_PREFIX() {
		return new SimpleDateFormat("dd.MM.yyyy").format(new Date()) + " - " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " - ";
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
	public static void scheduleRepeatingTask(final int delayMilliseconds, final String description, final Runnable r) {
		if (delayMilliseconds == 0) return;
		tasks.add(exe.submit(new Runnable() {

			public void run() {
				while (true) {
					try {
						long time = System.nanoTime();
						r.run();
						cpuTime += (System.nanoTime()-time);
						Thread.sleep(delayMilliseconds);
					} catch (InterruptedException e) {
						break;
					} catch (Exception e) {
						error("An error occured when " + description, e);
					} catch (Error e) {
						error("An error occured when " + description, e);
					}
				}
			}
		}));
	}
	public static void runTask(final String description, final Runnable r) {
		exe.submit(new Runnable() {

			public void run() {
				try {
					long time = System.nanoTime();
					r.run();
					cpuTime += (System.nanoTime()-time);
				} catch (Exception e) {
					error("An error occured when " + description, e);
				} catch (Error e) {
					error("An error occured when " + description, e);
				}
			}
		});
	}
	public static void runTaskLater(final int delayMilliseconds, final String description, final Runnable r) {
		final Future<?>[] array = new Future[1];
		array[0] = exe.submit(new Runnable() {


			public void run() {
				try {
					Thread.sleep(delayMilliseconds);
					long time = System.nanoTime();
					r.run();
					cpuTime += (System.nanoTime()-time);
				} catch (InterruptedException e) {
				} catch (Exception e) {
					error("An error occured when " + description, e);
				} catch (Error e) {
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
	public static void uninject(final ITabPlayer player) {
		player.getChannel().eventLoop().execute(new Runnable() {


			public void run() {
				try {
					player.getChannel().pipeline().remove(DECODER_NAME);
				} catch (Exception e){

				}
			}
		});
	}
	public static enum ServerType{
		BUKKIT, BUNGEE;
	}
}