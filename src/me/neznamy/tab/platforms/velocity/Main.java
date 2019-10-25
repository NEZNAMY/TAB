package me.neznamy.tab.platforms.velocity;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

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
import me.neznamy.tab.shared.Shared.CPUSample;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;

@Plugin(id = "tab", name = "TAB", version = "2.5.3", description = "Change a player's tablist prefix/suffix, name tag prefix/suffix, header/footer, bossbar and more", authors = {"NEZNAMY"})
public class Main implements MainClass{

	public static ProxyServer server;
	public static Logger logger;
	public static boolean disabled;

	@Inject
	public Main(ProxyServer server, Logger logger) {
		Main.server = server;
		Main.logger = logger;
	}
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		long time = System.currentTimeMillis();
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.BUNGEE;
		Shared.mainClass = this;
		server.getCommandManager().register("btab", new Command() {
			public void execute(CommandSource sender, String[] args) {
				TabCommand.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
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
	@Subscribe
	public void a(DisconnectEvent e){
		if (disabled) return;
		ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return; //player connected to bungeecord successfully, but not to the bukkit server anymore
		Placeholders.recalculateOnlineVersions();
		NameTag16.playerQuit(disconnectedPlayer);
		ScoreboardManager.unregister(disconnectedPlayer);
		Shared.data.remove(e.getPlayer().getUniqueId());
		if (Configs.SECRET_remove_ghost_players) {
			Object packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, disconnectedPlayer.getInfoData()).toVelocity(null);
			for (ITabPlayer all : Shared.getPlayers()) {
				all.sendPacket(packet);
			}
		}
	}
	@Subscribe
	public void a(ServerConnectedEvent e){
		try{
			if (disabled) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) {
				p = new TabPlayer(e.getPlayer(), e.getServer().getServerInfo().getName());
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				inject(p.getUniqueId());
				Placeholders.recalculateOnlineVersions();
				HeaderFooter.playerJoin(p);
				TabObjective.playerJoin(p);
				BossBar.playerJoin(p);
				ScoreboardManager.register(p);
				ITabPlayer pl = p;
				NameTag16.playerJoin(pl);
			} else {
				String from = p.getWorldName();
				String to = p.world = e.getPlayer().getCurrentServer().get().getServerInfo().getName();
				p.onWorldChange(from, to);
			}
		} catch (Throwable ex){
			Shared.error(null, "An error occured when player joined/changed server", ex);
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
	private void inject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

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
						PacketPlayOutPlayerInfo p = PacketPlayOutPlayerInfo.fromVelocity(packet);
						Playerlist.modifyPacket(p, player);
						packet = p.toVelocity(null);
					}
/*					if (packet instanceof Team && NameTag16.enable) {
						if (killPacket(packet)) return;
					}*/ //missing packets on velocity
				} catch (Throwable e){
					Shared.error(null, "An error occured when analyzing packets", e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	public void sendConsoleMessage(String message) {
		server.getConsoleCommandSource().sendMessage(TextComponent.of(message));
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
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) {
		return packet.toVelocity(protocolVersion);
	}
	public void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("velocityconfig.yml", "config.yml", Configs.configComments);
		Playerlist.refresh = Configs.config.getInt("tablist-refresh-interval-milliseconds", 1000);
		HeaderFooter.refresh = Configs.config.getInt("header-footer-refresh-interval-milliseconds", 50);
		TabObjective.type = TabObjectiveType.NONE;
	}
	//java class loader is `intelligent` and throws NoClassDefFoundError in inactive code (PacketPlayOutPlayerInfo#toVelocity)
	//making it return Object and then casting fixes it
	public static Object componentFromText(String text) {
		if (text == null) return null;
		return TextComponent.of(text);
	}
	public static String textFromComponent(Component component) {
		if (component == null) return null;
		return ((TextComponent) component).content();
	}
	public static void registerPlaceholders() {
		Placeholders.serverPlaceholders = new ArrayList<ServerPlaceholder>();
		Placeholders.playerPlaceholders = new ArrayList<PlayerPlaceholder>();
		Shared.registerUniversalPlaceholders();
		Placeholders.serverPlaceholders.add(new ServerPlaceholder("%maxplayers%", 1000) {
			public String get() {
				return server.getConfiguration().getShowMaxPlayers()+"";
			}
		});
		for (Entry<String, String> servers : server.getConfiguration().getServers().entrySet()) {
			Placeholders.serverPlaceholders.add(new ServerPlaceholder("%online_" + servers.getKey() + "%", 1000) {
				public String get() {
					return server.getServer(servers.getKey()).get().getPlayersConnected().size()+"";
				}
			});
		}
	}
}