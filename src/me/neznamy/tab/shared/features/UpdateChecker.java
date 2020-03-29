package me.neznamy.tab.shared.features;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Shared;

public class UpdateChecker {

	private static final int currentVersionId = 272;
	
	public UpdateChecker() {
		new Thread(new Runnable() {

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
						Shared.mainClass.sendConsoleMessage("&a[TAB] Version " + versionString + " is out! Your version: " + Shared.pluginVersion);
						if (Premium.is()) {
							Shared.mainClass.sendConsoleMessage("&a[TAB] Get the update at https://www.mc-market.org/resources/14009/");
						} else {
							Shared.mainClass.sendConsoleMessage("&a[TAB] Get the update at https://www.spigotmc.org/resources/57806/");
						}
					}
				} catch (Exception e) {
//					Shared.mainClass.sendConsoleMessage("&a[TAB] Failed to check for updates (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
				}
			}
		}).run();
	}
}