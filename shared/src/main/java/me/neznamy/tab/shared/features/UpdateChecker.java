package me.neznamy.tab.shared.features;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.neznamy.tab.shared.TAB;

/**
 * Update checker
 */
public class UpdateChecker {

	//separate field to prevent false flag on pre releases
	private String currentRelease = "2.9.2";
	
	public UpdateChecker(TAB tab) {
		new Thread(() -> {
			try {
				HttpURLConnection con = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=57806").openConnection();
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String versionString = br.readLine();
				br.close();
				con.disconnect();
				if (!versionString.equals(currentRelease)) {
					tab.getPlatform().sendConsoleMessage("&a[TAB] Version " + versionString + " is out! Your version: " + tab.getPluginVersion(), true);
					tab.getPlatform().sendConsoleMessage("&a[TAB] Get the update at https://www.spigotmc.org/resources/57806/", true);
				}
			} catch (Exception e) {
				tab.debug("&cFailed to check for updates (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
			}
		}).start();
	}
}