package me.neznamy.tab.platforms.velocity;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.protocol.packet.PlayerListItem;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.Channel;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;

@Plugin(id = "tab", name = "TAB", version = "2.5.2-pre14", description = "Change a player's tablist prefix/suffix, name tag prefix/suffix, header/footer, bossbar and more", authors = {"NEZNAMY"})
public class Main implements MainClass{

	public static ProxyServer server;
	public static Logger logger;
	public static Main instance;
	public static boolean disabled = false;

	@Inject
	public Main(ProxyServer server, Logger logger) {
		Main.server = server;
		Main.logger = logger;
	}
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		long time = System.currentTimeMillis();
		instance = this;
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.UNKNOWN;
		Shared.init(this, "2.5.2-pre14");
		server.getCommandManager().register("btab", new Command() {
			public void execute(CommandSource sender, String[] args) {
				TabCommand.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUsername()) : null, args);
			}
		});
		load(false, true);
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
			Shared.error("Failed to unload the plugin", e);
		}
	}
	public void load(boolean broadcastTime, boolean inject) {
		try {
			disabled = false;
			long time = System.currentTimeMillis();
			Shared.startupWarns = 0;
			Configs.loadFiles();
			registerPlaceholders();
			Shared.data.clear();
			for (Player p : server.getAllPlayers()) {
				ITabPlayer t = new TabPlayer(p, p.getCurrentServer().get().getServerInfo().getName());
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
		} catch (Throwable e1) {
			Shared.print("§c", "Did not enable.");
			e1.printStackTrace();
			Shared.error(null, e1);
			disabled = true;
		}
	}
	@Subscribe
	public void a(DisconnectEvent e){
		if (disabled) return;
		ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return; //player connected to bungeecord successfully, but not to the bukkit server anymore
		Placeholders.recalculateOnlineVersions();
		NameTag16.playerQuit(disconnectedPlayer);
		ScoreboardManager.unregister(disconnectedPlayer);
		Shared.data.remove(e.getPlayer().getUniqueId());
	}
	@Subscribe
	public void a(final ServerConnectedEvent e){
		try{
			if (disabled) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) {
				p = new TabPlayer(e.getPlayer(), e.getServer().getServerInfo().getName());
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				inject(p.getUniqueId());
				p.updatePlayerListName(false);
				Placeholders.recalculateOnlineVersions();
				HeaderFooter.playerJoin(p);
				TabObjective.playerJoin(p);
				BossBar.playerJoin(p);
				ScoreboardManager.register(p);
				final ITabPlayer pl = p;
				NameTag16.playerJoin(pl);
			} else {
				String from = p.getWorldName();
				String to = e.getPlayer().getCurrentServer().get().getServerInfo().getName();
				p.world = e.getPlayer().getCurrentServer().get().getServerInfo().getName();
				p.onWorldChange(from, to);
			}
		} catch (Throwable ex){
			Shared.error("An error occured when player joined/changed server", ex);
		}
	}
	@Subscribe
	public void a(PlayerChatEvent e) {
		ITabPlayer sender = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (e.getMessage().equalsIgnoreCase("/btab")) {
			Shared.sendPluginInfo(sender);
			return;
		}
		if (BossBar.onChat(sender, e.getMessage())) e.setResult(ChatResult.denied());
		if (ScoreboardManager.onCommand(sender, e.getMessage())) e.setResult(ChatResult.denied());
	}
	private void inject(final UUID uuid) {
		((Channel) Shared.getPlayer(uuid).getChannel()).pipeline().addBefore("handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				super.channelRead(context, packet);
			}
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				try{
					final ITabPlayer player = Shared.getPlayer(uuid);
					if (player == null) {
						//wtf
						super.write(context, packet, channelPromise);
						return;
					}
					if (packet instanceof PlayerListItem && Playerlist.enable) {
						Playerlist.modifyPacket((PlayerListItem) packet, player);
					}
/*					if (packet instanceof Team && NameTag16.enable) {
						if (killPacket(packet)) return;
					}*/
				} catch (Throwable e){
					Shared.error("An error occured when analyzing packets", e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	public Component createComponent(String text) {
		return TextComponent.of(text);
	}
	public void sendConsoleMessage(String message) {
		server.getConsoleCommandSource().sendMessage(createComponent(message));
	}
	public boolean listNames() {
		return Playerlist.enable;
	}
	public String getPermissionPlugin() {
		if (server.getPluginManager().getPlugin("LuckPerms") != null) return "LuckPerms";
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
/*		if (((Team) packetPlayOutScoreboardTeam).getFriendlyFire() != 69) {
			String[] players = ((Team) packetPlayOutScoreboardTeam).getPlayers();
			if (players == null) return false;
			for (ITabPlayer p : Shared.getPlayers()) {
				for (String player : players) {
					if (player.equals(p.getName()) && !p.disabledNametag) {
						return true;
					}
				}
			}
		}*/
		return false;
	}
	public Object toNMS(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) {
		return packet.toVelocity(protocolVersion);
	}
	public void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("velocityconfig.yml", "config.yml");
		Playerlist.refresh = Configs.config.getInt("tablist-refresh-interval-milliseconds", 1000);
		Playerlist.enable = Configs.config.getBoolean("change-tablist-prefix-suffix", true);
		HeaderFooter.refresh = Configs.config.getInt("header-footer-refresh-interval-milliseconds", 50);
		TabObjective.type = TabObjectiveType.NONE;
	}
	public static void registerPlaceholders() {
		Placeholders.list = new ArrayList<Placeholder>();
		Shared.registerUniversalPlaceholders();
		Placeholders.list.add(new Placeholder("%maxplayers%") {
			public String get(ITabPlayer p) {
				return server.getConfiguration().getShowMaxPlayers()+"";
			}
		});
		for (final Entry<String, String> servers : server.getConfiguration().getServers().entrySet()) {
			Placeholders.list.add(new Placeholder("%online_" + servers.getKey() + "%") {
				public String get(ITabPlayer p) {
					return server.getServer(servers.getKey()).get().getPlayersConnected().size()+"";
				}
			});
		}
	}
}