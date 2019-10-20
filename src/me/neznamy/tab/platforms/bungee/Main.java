package me.neznamy.tab.platforms.bungee;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import io.netty.channel.*;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.Shared.CPUSample;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

public class Main extends Plugin implements Listener, MainClass{

	public static boolean disabled;

	public void onEnable(){
		long time = System.currentTimeMillis();
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.BUNGEE;
		Shared.mainClass = this;
		getProxy().getPluginManager().registerListener(this, this);
		getProxy().getPluginManager().registerCommand(this, new Command("btab") {
			public void execute(CommandSender sender, String[] args) {
				TabCommand.execute(sender instanceof ProxiedPlayer ? Shared.getPlayer(((ProxiedPlayer)sender).getUniqueId()) : null, args);
			}
		});
		load(false, true);
		Metrics metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.SimplePie("permission_system", new Callable<String>() {
			public String call() {
				return getPermissionPlugin();
			}
		}));
		if (!disabled) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-time) + "ms");
	}
	public void onDisable() {
		if (!disabled) {
			for (ITabPlayer p : Shared.getPlayers()) ((Channel) p.getChannel()).pipeline().remove(Shared.DECODER_NAME);
			unload();
		}
	}
	public void unload() {
		try {
			if (disabled) return;
			long time = System.currentTimeMillis();
			Shared.cancelAllTasks();
			Configs.animations = null;
			HeaderFooter.unload();
			TabObjective.unload();
			Playerlist.unload();
			NameTag16.unload();
			BossBar.unload();
			ScoreboardManager.unload();
			Shared.data.clear();
			Shared.print("§a", "Disabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Throwable e) {
			Shared.error(null, "Failed to unload the plugin", e);
		}
	}
	public void load(boolean broadcastTime, boolean inject) {
		try {
			disabled = false;
			long time = System.currentTimeMillis();
			Shared.startupWarns = 0;
			Shared.cpuHistory = new ArrayList<CPUSample>();
			Configs.loadFiles();
			registerPlaceholders();
			Shared.data.clear();
			for (ProxiedPlayer p : getProxy().getPlayers()) {
				ITabPlayer t = new TabPlayer(p);
				Shared.data.put(p.getUniqueId(), t);
				if (inject) inject(t.getUniqueId());
			}
			Placeholders.recalculateOnlineVersions();
			BossBar.load();
			NameTag16.load();
			Playerlist.load();
			TabObjective.load();
			HeaderFooter.load();
			ScoreboardManager.load();
			Shared.startCPUTask();
			if (Shared.startupWarns > 0) Shared.print("§e", "There were " + Shared.startupWarns + " startup warnings.");
			if (broadcastTime) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (ParserException | ScannerException e) {
			Shared.print("§c", "Did not enable due to a broken configuration file.");
			disabled = true;
		} catch (Throwable e) {
			Shared.print("§c", "Failed to enable");
			sendConsoleMessage("§c" + e.getClass().getName() +": " + e.getMessage());
			for (StackTraceElement ste : e.getStackTrace()) {
				sendConsoleMessage("§c       at " + ste.toString());
			}
			disabled = true;
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerDisconnectEvent e){
		if (disabled) return;
		ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return; //player connected to bungeecord successfully, but not to the bukkit server anymore
		Placeholders.recalculateOnlineVersions();
		NameTag16.playerQuit(disconnectedPlayer);
		ScoreboardManager.unregister(disconnectedPlayer);
		Shared.data.remove(e.getPlayer().getUniqueId());
	}
/*	@EventHandler
	public void a(PostLoginEvent e) {
		if (disabled) return;
		System.out.println("------------------------------");
		System.out.println(e.getClass().getSimpleName());
		System.out.println("------------------------------");
		ITabPlayer p = new TabPlayer(e.getPlayer());
		Shared.data.put(e.getPlayer().getUniqueId(), p);
		inject(p);
	}
	@EventHandler
	public void a(ServerSwitchEvent e){
		if (disabled) return;
		System.out.println("------------------------------");
		System.out.println(e.getClass().getSimpleName());
		System.out.println("------------------------------");
		try{
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (((ProxiedPlayer)p.getPlayer()).getServer() == null) {
				System.out.println("server change");
				String from = p.getWorldName();
				String to = e.getPlayer().getServer().getInfo().getName();
				((TabPlayer)p).server = e.getPlayer().getServer();
				p.onWorldChange(from, to);
			} else {
				System.out.println("new join");
				p.onJoin();
				p.updatePlayerListName(false);
				Placeholders.recalculateOnlineVersions();
				NameTag16.playerJoin(p);
				HeaderFooter.playerJoin(p);
				TabObjective.playerJoin(p);
				BossBar.playerJoin(p);
				ScoreboardManager.register(p);
			}
		} catch (Throwable ex){
			Shared.error("An error occured when player joined/changed server", ex);
		}
	}*/
	@EventHandler
	public void a(ServerSwitchEvent e){
		try{
			if (disabled) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) {
				p = new TabPlayer(e.getPlayer());
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				inject(p.getUniqueId());
				Placeholders.recalculateOnlineVersions();
				HeaderFooter.playerJoin(p);
				TabObjective.playerJoin(p);
				BossBar.playerJoin(p);
				ScoreboardManager.register(p);
				NameTag16.playerJoin(p);
			} else {
				String from = p.getWorldName();
				String to = p.world = e.getPlayer().getServer().getInfo().getName();
				p.onWorldChange(from, to);
			}
			
		} catch (Throwable ex){
			Shared.error(null, "An error occured when player joined/changed server", ex);
		}
	}
	@EventHandler
	public void a(ChatEvent e) {
		ITabPlayer sender = Shared.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId());
		if (e.getMessage().equalsIgnoreCase("/btab")) {
			Shared.sendPluginInfo(sender);
			return;
		}
		if (BossBar.onChat(sender, e.getMessage())) e.setCancelled(true);
		if (ScoreboardManager.onCommand(sender, e.getMessage())) e.setCancelled(true);
	}
	private void inject(UUID uuid) {
		((Channel) Shared.getPlayer(uuid).getChannel()).pipeline().addBefore("inbound-boss", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				super.channelRead(context, packet);
			}
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				try{
					ITabPlayer player = Shared.getPlayer(uuid);
					if (player == null) {
						//wtf
						super.write(context, packet, channelPromise);
						return;
					}
					if (packet instanceof PlayerListItem && Playerlist.enable && player.getVersion().getMinorVersion() >= 8) {
						PacketPlayOutPlayerInfo p = PacketPlayOutPlayerInfo.fromBungee(packet);
						Playerlist.modifyPacket(p, player);
						packet = p.toBungee(null);
					}
					if (packet instanceof Team && NameTag16.enable) {
						if (killPacket(packet)) return;
					}
				} catch (Throwable e){
					Shared.error(null, "An error occured when analyzing packets", e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	@SuppressWarnings("deprecation")
	public void sendConsoleMessage(String message) {
		ProxyServer.getInstance().getConsole().sendMessage(message);
	}
	public String getPermissionPlugin() {
		if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) return "LuckPerms";
		if (ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms") != null) return "BungeePerms";
		return "Unknown/None";
	}
	public String getSeparatorType() {
		return "server";
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void reload(ITabPlayer sender) {
		unload();
		load(true, false);
		if (!disabled) TabCommand.sendMessage(sender, Configs.reloaded);
	}
	public boolean killPacket(Object packetPlayOutScoreboardTeam){
		if (((Team) packetPlayOutScoreboardTeam).getFriendlyFire() != 69) {
			String[] players = ((Team) packetPlayOutScoreboardTeam).getPlayers();
			if (players == null) return false;
			for (ITabPlayer p : Shared.getPlayers()) {
				for (String player : players) {
					if (player.equals(p.getName()) && !p.disabledNametag) {
						return true;
					}
				}
			}
		}
		return false;
	}
	public Object toNMS(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) {
		return packet.toBungee(protocolVersion);
	}
	public void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("bungeeconfig.yml", "config.yml", Configs.configComments);
		TabObjective.rawValue = Configs.config.getString("tablist-objective-value", "%ping%");
		TabObjective.type = (TabObjective.rawValue.length() == 0) ? TabObjectiveType.NONE : TabObjectiveType.CUSTOM;
		BelowName.enable = Configs.config.getBoolean("belowname.enabled", true);
		BelowName.refresh = Configs.config.getInt("belowname.refresh-interval", 200);
		BelowName.number = Configs.config.getString("belowname.number", "%health%");
		BelowName.text = Configs.config.getString("belowname.text", "Health");
		Playerlist.refresh = Configs.config.getInt("tablist-refresh-interval-milliseconds", 1000);
		NameTag16.enable = Configs.config.getBoolean("change-nametag-prefix-suffix", true);
		NameTag16.refresh = Configs.config.getInt("nametag-refresh-interval-milliseconds", 1000);
		HeaderFooter.refresh = Configs.config.getInt("header-footer-refresh-interval-milliseconds", 50);
	}
	public static void registerPlaceholders() {
		Placeholders.serverPlaceholders = new ArrayList<Placeholder>();
		Placeholders.playerPlaceholders = new ArrayList<Placeholder>();
		Shared.registerUniversalPlaceholders();
		Placeholders.serverPlaceholders.add(new Placeholder("%maxplayers%") {
			public String get(ITabPlayer p) {
				return ProxyServer.getInstance().getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers()+"";
			}
		});
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			Placeholders.serverPlaceholders.add(new Placeholder("%online_" + server.getKey() + "%") {
				public String get(ITabPlayer p) {
					return server.getValue().getPlayers().size()+"";
				}
			});
		}
	}
}