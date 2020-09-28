package me.neznamy.tab.platforms.bukkit.features;

import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.shared.Shared;

/**
 * Utility to execute /papi ecloud download <expansion> on expansions which are used but not installed
 * If an expansion was successfully downloaded (new expansion count is higher than previous in expansions folder),
 * /papi reload is executed.
 * Since this feature was added into the plugin amount of non-working placeholders caused by missing expansion went down rapidly.
 * I'm not sure why, must be a coinscidence.
 */
public class ExpansionDownloader {

	private JavaPlugin plugin;
	
	public ExpansionDownloader(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void download(Set<String> expansions) {
		//starting the task once the server is fully loaded (including PlaceholderAPI expansions)
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

			@Override
			public void run() {
				//to not freeze the server with Thread.sleep
				Shared.cpu.runTask("Downloading PlaceholderAPI Expansions", new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(5000);
							Shared.debug("Used expansions: " + expansions.toString());
							Shared.debug("Registered expansions in PlaceholderAPI: " + PlaceholderAPI.getRegisteredIdentifiers());
							expansions.removeAll(PlaceholderAPI.getRegisteredIdentifiers());
							Shared.debug("Expansions to install: " + expansions.toString());
							if (!expansions.isEmpty()) {
								File expansionsFolder = new File(Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDataFolder(), "expansions");
								int oldExpansionDownloadedCount = expansionsFolder.listFiles().length;
								for (String expansion : expansions) {
									Bukkit.getConsoleSender().sendMessage("\u00a7d[TAB] Expansion \u00a7e" + expansion + "\u00a7d is used but not installed. Installing!");
									runSyncCommand("papi ecloud download " + expansion);
									Thread.sleep(5000);
								}
								if (expansionsFolder.listFiles().length > oldExpansionDownloadedCount) {
									Bukkit.getConsoleSender().sendMessage("\u00a7d[TAB] Reloading PlaceholderAPI for the changes to take effect.");
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
	private void runSyncCommand(String command) {
		//back to main thread as commands need to be ran in it
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

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