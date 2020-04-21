package me.neznamy.tab.platforms.bukkit.features;

import java.io.File;
import java.util.ConcurrentModificationException;

import org.bukkit.Bukkit;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.shared.Shared;

public class ExpansionDownloader{

	public ExpansionDownloader() {
		Main instance = Main.instance;
		Bukkit.getScheduler().runTaskLaterAsynchronously(instance, new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(5000);
					Shared.debug("All used expansions: " + Main.usedExpansions);
					Shared.debug("Installed expansions: " + PlaceholderAPI.getRegisteredIdentifiers());
					Main.usedExpansions.removeAll(PlaceholderAPI.getRegisteredIdentifiers());
					Shared.debug("Expansions to install: " + Main.usedExpansions);
					if (!Main.usedExpansions.isEmpty()) {
						File expansionsFolder = new File("plugins" + File.separatorChar + "PlaceholderAPI" + File.separatorChar + "expansions");
						int oldExpansionDownloadedCount = expansionsFolder.listFiles().length;
						for (String expansion : Main.usedExpansions) {
							instance.sendConsoleMessage("&d[TAB] Expansion &e" + expansion + "&d is used but not installed. Installing!");
							Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {

								@Override
								public void run() {
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "papi ecloud download " + expansion);
								}
							});
							Thread.sleep(5000);
						}
						if (expansionsFolder.listFiles().length > oldExpansionDownloadedCount) {
							instance.sendConsoleMessage("&d[TAB] Reloading PlaceholderAPI for the changes to take effect");
							Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {

								@Override
								public void run() {
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "papi reload");
								}
							});
						}
					}
				} catch (InterruptedException | ConcurrentModificationException e) {
				} catch (Throwable e) {
					Shared.errorManager.printError("Failed to download PlaceholderAPI expansions. PlaceholderAPI version: " + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion(), e);
				}
			}
		}, 1);
	}
}