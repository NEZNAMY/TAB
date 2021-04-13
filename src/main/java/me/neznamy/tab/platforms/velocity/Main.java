package me.neznamy.tab.platforms.velocity;

import java.util.ArrayList;
import java.util.List;

import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import me.neznamy.injector.VelocityPacketRegistry;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * Main class for Velocity platform
 */
@Plugin(id = "tab", name = "TAB", version = TAB.pluginVersion, description = "An all-in-one solution that works", authors = {"NEZNAMY"})
public class Main {

	//instance of proxyserver
	public ProxyServer server;
	
	//metrics factory I guess
	private Metrics.Factory metricsFactory;

	//plugin message handler
	public static PluginMessageHandler plm;

	@Inject
	public Main(ProxyServer server, Metrics.Factory metricsFactory) {
		this.server = server;
		this.metricsFactory = metricsFactory;
	}

	/**
	 * Initializes plugin for velocity
	 * @param event - velocity initialize event
	 */
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		if (!isVersionSupported()) {
			System.out.println("\u00a7c[TAB] The plugin requires Velocity 1.1.0 and up to work. Get it at https://velocitypowered.com/downloads");
			return;
		}
		if (!new VelocityPacketRegistry().registerPackets()) {
			System.out.println("\u00a7c[TAB] Your velocity version is way too new for this plugin version. Update the plugin or downgrade Velocity.");
			return;
		}
		if (server.getConfiguration().isOnlineMode()) {
			System.out.println("\u00a76[TAB] If you experience tablist prefix/suffix not working and global playerlist duplicating players, toggle "
					+ "\"use-online-uuid-in-tablist\" option in config.yml (set it to opposite value).");
		}
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.values()[1];
		TAB.setInstance(new TAB(new VelocityPlatform(server), new VelocityPacketBuilder()));
		server.getEventManager().register(this, new VelocityEventListener());
		server.getCommandManager().register(server.getCommandManager().metaBuilder("btab").build(), new VelocityTABCommand());
		server.getCommandManager().register(server.getCommandManager().metaBuilder("vtab").build(), new VelocityTABCommand());
		plm = new VelocityPluginMessageHandler(this);
		TAB.getInstance().load();
		Metrics metrics = metricsFactory.make(this, 10533);
		metrics.addCustomChart(new SimplePie("global_playerlist_enabled", () -> TAB.getInstance().getFeatureManager().isFeatureEnabled("globalplayerlist") ? "Yes" : "No"));
		metrics.addCustomChart(new SimplePie("using_premium_version", () -> TAB.getInstance().isPremium() ? "Yes" : "No"));
	}

	/**
	 * Checks for compatibility and returns true if version is supported, false if not
	 * @return true if version is compatible, false if not
	 */
	private boolean isVersionSupported() {
		try {
			Class.forName("org.yaml.snakeyaml.Yaml"); //1.1.0+
			Class.forName("net.kyori.adventure.identity.Identity"); //1.1.0 b265
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Unloads the plugin
	 * @param event - proxy disable event
	 */
	@Subscribe
	public void onProxyInitialization(ProxyShutdownEvent event) {
		if (TAB.getInstance() != null) TAB.getInstance().unload();
	}
	
	public static Component stringToComponent(String string) {
		if (string == null) return null;
		return GsonComponentSerializer.gson().deserialize(string);
	}
	
	public static class VelocityTABCommand implements Command {
		
		@Override
		public void execute(CommandSource sender, String[] args) {
			if (TAB.getInstance().isDisabled()) {
				for (String message : TAB.getInstance().disabledCommand.execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
					sender.sendMessage(Identity.nil(), Component.text(message.replace('&', '\u00a7')));
				}
			} else {
				TAB.getInstance().command.execute(sender instanceof Player ? TAB.getInstance().getPlayer(((Player)sender).getUniqueId()) : null, args);
			}
		}

		@Override
		public List<String> suggest(CommandSource sender, String[] args) {
			if (TAB.getInstance().isDisabled()) return new ArrayList<String>();
			return TAB.getInstance().command.complete(sender instanceof Player ? TAB.getInstance().getPlayer(((Player)sender).getUniqueId()) : null, args);
		}
	}
}