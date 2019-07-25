package me.neznamy.tab.bungee;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.shared.BossBar;
import me.neznamy.tab.shared.BossBar.BossBarLine;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.HeaderFooter;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.MainClass;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Placeholders;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.ServerType;
import me.neznamy.tab.shared.TabCommand;
import me.neznamy.tab.shared.TabObjective;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

@SuppressWarnings("deprecation")
public class Main extends Plugin implements Listener, MainClass{

	public static Main instance;
	public static boolean disabled = false;

	public void onEnable(){
		long time = System.currentTimeMillis();
		instance = this;
		Shared.init(this, ServerType.BUNGEE, getDescription().getVersion());
		Placeholders.maxPlayers = ProxyServer.getInstance().getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers();
		getProxy().getPluginManager().registerListener(this, this);
		getProxy().getPluginManager().registerCommand(this, new Command("btab") {

			
			public void execute(CommandSender sender, String[] args) {
				TabCommand.execute(sender instanceof ProxiedPlayer ? Shared.getPlayer(sender.getName()) : null, args);
			}
		});
		load(false, true);
		if (!disabled) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-time) + "ms");
	}
	public void onDisable() {
		if (!disabled) {
			for (ITabPlayer p : Shared.getPlayers()) Shared.uninject(p);
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
			Shared.data.clear();
			Shared.print("§a", "Disabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Exception e) {
			Shared.error("Failed to unload the plugin", e);
		}
	}
	public void load(boolean broadcastTime, boolean inject) {
		try {
			disabled = false;
			long time = System.currentTimeMillis();
			Configs.loadFiles();
			Shared.data.clear();
			for (ProxiedPlayer p : getProxy().getPlayers()) {
				ITabPlayer t = new TabPlayer(p);
				Shared.data.put(p.getUniqueId(), t);
				if (inject) inject(t);
			}
			for (ITabPlayer p : Shared.getPlayers()) p.updatePlayerListName(false);
			Placeholders.recalculateOnlineVersions();
			BossBar.load();
			NameTag16.load();
			Playerlist.load();
			TabObjective.load();
			HeaderFooter.load();
			Shared.startCPUTask();
			if (broadcastTime) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Exception e1) {
			Shared.print("§c", "Did not enable.");
			disabled = true;
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerDisconnectEvent e){
		if (disabled) return;
		ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		Placeholders.recalculateOnlineVersions();
		if (disconnectedPlayer != null) {
			NameTag16.playerQuit(disconnectedPlayer);
			Shared.data.remove(e.getPlayer().getUniqueId());
		}
	}
	@EventHandler
	public void a(ServerSwitchEvent e){
		try{
			if (disabled) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) {
				p = new TabPlayer(e.getPlayer());
				p.updatePlayerListName(false);
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				inject(p);
				Placeholders.recalculateOnlineVersions();
				HeaderFooter.playerJoin(p);
				TabObjective.playerJoin(p);
				NameTag16.playerJoin(p);
				BossBar.playerJoin(p);
			} else {
				String from = p.getWorldName();
				String to = e.getPlayer().getServer().getInfo().getName();
				((TabPlayer)p).server = e.getPlayer().getServer();
				p.updateGroupIfNeeded();
				p.updateAll();
				if (BossBar.enable) {
					if (Configs.disabledBossbar.contains(to)) {
						for (BossBarLine line : BossBar.lines) PacketAPI.removeBossBar(p, line.getBossBar());
					}
					if (!Configs.disabledBossbar.contains(to) && Configs.disabledBossbar.contains(from)) {
						for (BossBarLine line : BossBar.lines) BossBar.sendBar(p, line);
					}
				}
				if (HeaderFooter.enable) {
					if (Configs.disabledHeaderFooter.contains(to)) {
						new PacketPlayOutPlayerListHeaderFooter("","").send(p);
					} else {
						HeaderFooter.refreshHeaderFooter(p);
					}
				}
				if (NameTag16.enable) {
					if (Configs.disabledNametag.contains(to)) {
						p.unregisterTeam();
					} else {
						p.registerTeam();
					}
				}
				if (listNames()) p.updatePlayerListName(false);
				if (TabObjective.type != TabObjectiveType.NONE) {
					if (Configs.disabledTablistObjective.contains(to) && !Configs.disabledTablistObjective.contains(from)) {
						TabObjective.unload(p);
					}
					if (!Configs.disabledTablistObjective.contains(to) && Configs.disabledTablistObjective.contains(from)) {
						TabObjective.playerJoin(p);
					}
				}
			}
		} catch (Exception ex){
			Shared.error("An error occured when player joined/changed server", ex);
		}
	}
	private void inject(final ITabPlayer player) {
		player.getChannel().pipeline().addBefore("inbound-boss", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				try{
					PacketWrapper wrapper = (PacketWrapper) packet;
					if (wrapper.packet != null && wrapper.packet instanceof Chat) {
						if (((Chat)wrapper.packet).getMessage().equalsIgnoreCase("/btab")) {
							sendPluginInfo(player);
						}
						if (BossBar.onChat(player, ((Chat)wrapper.packet).getMessage())) return;
					}
				} catch (Exception e){
					Shared.error("An error occured when analyzing packets", e);
				} catch (Error e){
					Shared.error("An error occured when analyzing packets", e);
				}
				super.channelRead(context, packet);
			}
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				try{
					if (packet instanceof PlayerListItem && Playerlist.enable) {
						if (!Configs.disabledTablistNames.contains(player.getWorldName()))
							Playerlist.modifyPacket((PlayerListItem) packet, player);
					}
					if (packet instanceof Team && NameTag16.enable) {
						if (!Configs.disabledNametag.contains(player.getWorldName()))
							if (killPacket(packet)) return;
					}
				} catch (Exception e){
					Shared.error("An error occured when analyzing packets", e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	public String createComponent(String text) {
		return ComponentSerializer.toString(new TextComponent(text));
	}
	public void sendPluginInfo(ITabPlayer to) {
		TextComponent component = new TextComponent("");
		TextComponent extra1 = new TextComponent("§3TAB v" + Shared.pluginVersion);
		extra1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aClick to visit plugin's spigot page").create()));
		extra1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/57806/"));
		TextComponent extra2 = new TextComponent(" §0by _NEZNAMY_ (discord: NEZNAMY#4659)");
		component.addExtra(extra1);
		component.addExtra(extra2);
		((ProxiedPlayer)to.getPlayer()).sendMessage(component);
	}
	public void sendConsoleMessage(String message) {
		ProxyServer.getInstance().getConsole().sendMessage(message);
	}
	public boolean listNames() {
		return Playerlist.enable;
	}
	public String getPermissionPlugin() {
		if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) return "LuckPerms";
		if (ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms") != null) return "BungeePerms";
		return "-";
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
	public boolean killPacket(Object packetPlayOutScoreboardTeam) throws Exception {
		if (((Team) packetPlayOutScoreboardTeam).getFriendlyFire() != 69) {
			String[] players = ((Team) packetPlayOutScoreboardTeam).getPlayers();
			if (players == null) return false;
			for (ITabPlayer p : Shared.getPlayers()) {
				for (String player : players) {
					if (player.equals(p.getName())) return true;
				}
			}
		}
		return false;
	}
	public Object toNMS(UniversalPacketPlayOut packet, int protocolVersion) {
		return packet.toBungee(protocolVersion);
	}
}