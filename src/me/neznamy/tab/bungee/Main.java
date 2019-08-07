package me.neznamy.tab.bungee;

import io.netty.channel.*;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.Shared.ServerType;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.*;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

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
		new Metrics(this);
		if (!disabled) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-time) + "ms");
	}
	public void onDisable() {
		if (!disabled) {
			for (ITabPlayer p : Shared.getPlayers()) p.getChannel().pipeline().remove(Shared.DECODER_NAME);
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
			if (Shared.startupWarns > 0) Shared.print("§e", "There were " + Shared.startupWarns + " startup warnings.");
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
		NameTag16.playerQuit(disconnectedPlayer);
		Shared.data.remove(e.getPlayer().getUniqueId());
	}
	@EventHandler
	public void a(ServerSwitchEvent e){
		try{
			if (disabled) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) {
				p = new TabPlayer(e.getPlayer());
				inject(p);
				p.updatePlayerListName(false);
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				Placeholders.recalculateOnlineVersions();
				HeaderFooter.playerJoin(p);
				TabObjective.playerJoin(p);
				NameTag16.playerJoin(p);
				BossBar.playerJoin(p);
			} else {
				String from = p.getWorldName();
				String to = e.getPlayer().getServer().getInfo().getName();
				((TabPlayer)p).server = e.getPlayer().getServer();
				p.onWorldChange(from, to);
			}
		} catch (Exception ex){
			Shared.error("An error occured when player joined/changed server", ex);
		}
	}
	@EventHandler
	public void a(ChatEvent e) {
		ITabPlayer sender = Shared.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId());
		if (e.getMessage().equalsIgnoreCase("/btab")) {
			sendPluginInfo(sender);
			return;
		}
		if (BossBar.onChat(sender, e.getMessage())) e.setCancelled(true);
	}
	private void inject(final ITabPlayer player) {
		player.getChannel().pipeline().addBefore("inbound-boss", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				super.channelRead(context, packet);
			}
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				try{
					if (packet instanceof PlayerListItem && Playerlist.enable) {
						if (!player.disabledTablistNames) Playerlist.modifyPacket((PlayerListItem) packet, player);
					}
					if (packet instanceof Team && NameTag16.enable) {
						if (!player.disabledNametag && killPacket(packet)) return;
					}
				} catch (Exception e){
					Shared.error("An error occured when analyzing packets", e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	public String createComponent(String text) {
		if (text == null || text.length() == 0) return "{\"translate\":\"\"}";
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
	@SuppressWarnings("deprecation")
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
	public void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("bungeeconfig.yml", "config.yml");
		TabObjective.customValue = Configs.config.getString("tablist-objective-value", "%ping%");
		TabObjective.type = (TabObjective.customValue.length() == 0) ? TabObjectiveType.NONE : TabObjectiveType.CUSTOM;
		Playerlist.refresh = Configs.config.getInt("tablist-refresh-interval-milliseconds", 1000);
		Playerlist.enable = Configs.config.getBoolean("change-tablist-prefix-suffix", true);
		NameTag16.enable = Configs.config.getBoolean("change-nametag-prefix-suffix", true);
		NameTag16.refresh = Configs.config.getInt("nametag-refresh-interval-milliseconds", 1000);
		HeaderFooter.refresh = Configs.config.getInt("header-footer-refresh-interval-milliseconds", 50);
	}
}