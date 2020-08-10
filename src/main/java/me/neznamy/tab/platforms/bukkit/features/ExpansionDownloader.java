package me.neznamy.tab.platforms.bukkit.features;

import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Set;

import org.bukkit.Bukkit;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.shared.Shared;

public class ExpansionDownloader{

	public void download(String expansion) {
		Shared.featureCpu.runTask("Downloading PlaceholderAPI Expansions", new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					if (PlaceholderAPI.getRegisteredIdentifiers().contains(expansion)) return;
					File expansionsFolder = new File(Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDataFolder(), "expansions");
					int oldExpansionDownloadedCount = expansionsFolder.listFiles().length;
					Shared.platform.sendConsoleMessage("&d[TAB] Expansion &e" + expansion + "&d is used but not installed. Installing!");
					runSyncCommand("papi ecloud download " + expansion);
					Thread.sleep(5000);
					if (expansionsFolder.listFiles().length > oldExpansionDownloadedCount) {
						Shared.platform.sendConsoleMessage("&d[TAB] Reloading PlaceholderAPI for the changes to take effect.");
						runSyncCommand("papi reload");
					}
				} catch (InterruptedException | ConcurrentModificationException e) {
				} catch (Throwable e) {
					Shared.errorManager.printError("Failed to download PlaceholderAPI expansion. PlaceholderAPI version: " + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion(), e);
				}
			}
		});
	}

	public void download(Set<String> expansions) {
		//starting the task once the server is fully loaded (including PlaceholderAPI expansions)
		Bukkit.getScheduler().runTaskLater(Main.INSTANCE, new Runnable() {

			@Override
			public void run() {
				//to not freeze the server with Thread.sleep
				Shared.featureCpu.runTask("Downloading PlaceholderAPI Expansions", new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(2000);
							expansions.removeAll(PlaceholderAPI.getRegisteredIdentifiers());
							if (!expansions.isEmpty()) {
								File expansionsFolder = new File(Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDataFolder(), "expansions");
								int oldExpansionDownloadedCount = expansionsFolder.listFiles().length;
								for (String expansion : expansions) {
									Shared.platform.sendConsoleMessage("&d[TAB] Expansion &e" + expansion + "&d is used but not installed. Installing!");
									runSyncCommand("papi ecloud download " + expansion);
									Thread.sleep(5000);
								}
								if (expansionsFolder.listFiles().length > oldExpansionDownloadedCount) {
									Shared.platform.sendConsoleMessage("&d[TAB] Reloading PlaceholderAPI for the changes to take effect.");
									runSyncCommand("papi reload");
								}
							}
						} catch (InterruptedException | ConcurrentModificationException e) {
						} catch (Throwable e) {
							Shared.errorManager.printError("Failed to download PlaceholderAPI expansions. PlaceholderAPI version: " + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion(), e);
						}
					}
				});
			}
		}, 1);
	}
	public void runSyncCommand(String command) {
		//back to main thread as commands need to be ran in it
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.INSTANCE, new Runnable() {

			@Override
			public void run() {
				try {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
				} catch (Exception e) {
					//papi ecloud is disabled
				}
			}
		});
	}
}