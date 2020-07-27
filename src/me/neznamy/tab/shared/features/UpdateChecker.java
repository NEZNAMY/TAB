package me.neznamy.tab.shared.features;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Shared;

public class UpdateChecker {

	private final String currentVersion = "2.8.3";
	
	public UpdateChecker() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					HttpURLConnection con = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=57806").openConnection();
					BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String versionString = br.readLine();
					br.close();
					con.disconnect();
					if (!versionString.equals(currentVersion)) {
						Shared.mainClass.sendConsoleMessage("&a[TAB] Version " + versionString + " is out! Your version: " + Shared.pluginVersion);
						if (Premium.is()) {
							Shared.mainClass.sendConsoleMessage("&a[TAB] Get the update at https://www.mc-market.org/resources/14009/");
						} else {
							Shared.mainClass.sendConsoleMessage("&a[TAB] Get the update at https://www.spigotmc.org/resources/57806/");
						}
					}
				} catch (Exception e) {
					Shared.debug("&cFailed to check for updates (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
				}
			}
		}).start();
	}
}