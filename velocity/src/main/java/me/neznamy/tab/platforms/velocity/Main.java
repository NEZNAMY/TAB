package me.neznamy.tab.platforms.velocity;

import java.util.ArrayList;
import java.util.List;

import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * Main class for Velocity platform
 */
@Plugin(id = "tab", name = "TAB", version = TAB.PLUGIN_VERSION, description = "An all-in-one solution that works", authors = {"NEZNAMY"})
public class Main {

	//instance of proxyserver
	private ProxyServer server;
	
	//metrics factory I guess
	private Metrics.Factory metricsFactory;

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
			server.getConsoleCommandSource().sendMessage(Identity.nil(), Component.text("\u00a7c[TAB] The plugin requires Velocity 1.1.0 and up to work. Get it at https://velocitypowered.com/downloads"));
			return;
		}
		if (server.getConfiguration().isOnlineMode()) {
			server.getConsoleCommandSource().sendMessage(Identity.nil(), Component.text("\u00a76[TAB] If you experience tablist prefix/suffix not working and global playerlist duplicating players, toggle "
					+ "\"use-online-uuid-in-tablist\" option in config.yml (set it to opposite value)."));
		}
		PluginMessageHandler plm = new VelocityPluginMessageHandler(this);
		TAB.setInstance(new TAB(new VelocityPlatform(server, plm), new VelocityPacketBuilder(), ProtocolVersion.values()[1]));
		server.getEventManager().register(this, new VelocityEventListener(plm));
		VelocityTABCommand cmd = new VelocityTABCommand();
		server.getCommandManager().register(server.getCommandManager().metaBuilder("btab").build(), cmd);
		server.getCommandManager().register(server.getCommandManager().metaBuilder("vtab").build(), cmd);
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
	public void onProxyShutdown(ProxyShutdownEvent event) {
		if (TAB.getInstance() != null) TAB.getInstance().unload();
	}
	
	public static Component stringToComponent(String string) {
		if (string == null) return null;
		return GsonComponentSerializer.gson().deserialize(string);
	}

	public ProxyServer getServer() {
		return server;
	}

	public static class VelocityTABCommand implements SimpleCommand {

		@Override
		public void execute(Invocation invocation) {
			CommandSource sender = invocation.source();
			if (TAB.getInstance().isDisabled()) {
				for (String message : TAB.getInstance().getDisabledCommand().execute(invocation.arguments(), sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
					sender.sendMessage(Identity.nil(), Component.text(message.replace('&', '\u00a7')));
				}
			} else {
				TabPlayer p = null;
				if (sender instanceof Player) {
					p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
					if (p == null) return; //player not loaded correctly
				}
				TAB.getInstance().getCommand().execute(p, invocation.arguments());
			}
		}

		@Override
		public List<String> suggest(Invocation invocation) {
			TabPlayer p = null;
			if (invocation.source() instanceof Player) {
				p = TAB.getInstance().getPlayer(((Player)invocation.source()).getUniqueId());
				if (p == null) return new ArrayList<>(); //player not loaded correctly
			}
			return TAB.getInstance().getCommand().complete(p, invocation.arguments());
		}
	}
}