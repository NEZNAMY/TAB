package me.neznamy.tab.bukkit;

import java.util.*;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import com.github.cheesesoftware.PowerfulPermsAPI.PowerfulPermsPlugin;

import me.neznamy.tab.bukkit.packets.*;
import me.neznamy.tab.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.bukkit.packets.Packet.*;
import me.neznamy.tab.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.FancyMessage.*;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Shared.ServerType;
import me.neznamy.tab.shared.packets.*;

public class Main extends JavaPlugin implements Listener, MainClass{

	public static PowerfulPermsPlugin powerfulPerms;
	public static GroupManager groupManager;
	public static boolean luckPerms;
	public static boolean pex;
	public static Main instance;
	public static boolean disabled = false;

	public void onEnable(){
		long total = System.currentTimeMillis();
		if (me.neznamy.tab.bukkit.packets.PacketAPI.isVersionSupported()){
			instance = this;
			Shared.init(this, ServerType.BUKKIT, getDescription().getVersion());
			me.neznamy.tab.shared.Placeholders.maxPlayers = Bukkit.getMaxPlayers();
			Bukkit.getPluginManager().registerEvents(this, this);
			getCommand("tab").setExecutor(new CommandExecutor() {

				public boolean onCommand(CommandSender sender, Command c, String cmd, String[] args){
					TabCommand.execute(sender instanceof Player ? Shared.getPlayer(sender.getName()) : null, args);
					return false;
				}
			});
			load(false, true);
			if (!disabled) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-total) + "ms");
		} else {
			if (NMSClass.versionNumber < 8) {
				Shared.print("§c", "Your server version (" + NMSClass.version + ") is not supported - too old! Disabling...");
			} else {
				Shared.print("§c", "Your server version (" + NMSClass.version + ") is not supported - too new! Please update the plugin.");
			}
			Bukkit.getPluginManager().disablePlugin(this);
		}
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
			Configs.animations = new ArrayList<Animation>();
			PerWorldPlayerlist.unload();
			HeaderFooter.unload();
			TabObjective.unload();
			Playerlist.unload();
			NameTag16.unload();
			NameTagX.unload();
			BossBar.unload();
			Shared.data.clear();
			Shared.print("§a", "Disabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Exception e) {
			Shared.error("Failed to unload the plugin", e);
		}
	}
	public void load(boolean broadcastTime, boolean inject) {
		try {
			long time = System.currentTimeMillis();
			disabled = false;
			Shared.startupWarns = 0;
			Configs.loadFiles();
			Placeholders.initialize();
			Shared.data.clear();
			for (Player p : Bukkit.getOnlinePlayers()) {
				ITabPlayer t = new TabPlayer(p);
				Shared.data.put(p.getUniqueId(), t);
				if(inject) inject(t);
				t.onJoin();
			}
			for (ITabPlayer p : Shared.getPlayers()) p.updatePlayerListName(false);
			me.neznamy.tab.shared.Placeholders.recalculateOnlineVersions();
			BossBar.load();
			BossBar1_8.load();
			NameTagX.load();
			NameTag16.load();
			Playerlist.load();
			TabObjective.load();
			HeaderFooter.load();
			PerWorldPlayerlist.load();
			Shared.startCPUTask();
			if (Shared.startupWarns > 0) Shared.print("§e", "There were " + Shared.startupWarns + " startup warnings.");
			if (broadcastTime) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Exception e1) {
			Shared.print("§c", "Did not enable. Check errors.txt for more info.");
			Shared.error("Failed to load plugin", e1);
			disabled = true;
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void a(PlayerLoginEvent e) {
		try {
			if (disabled) return;
			if (e.getResult() == Result.ALLOWED) {
				final Player p = e.getPlayer();
				if (Shared.getPlayer(p.getName()) != null) {
					Shared.error("Data of " + p.getName() + " already exists but login event was not cancelled?");
					return;
				}
				Shared.data.put(p.getUniqueId(), new TabPlayer(p));
				Shared.runTaskLater(300, "checking if player is online", new Runnable() {


					public void run() {
						if (!p.isOnline()) {
							Shared.data.remove(p.getUniqueId());
						}
					}
				});
			}
		} catch (Exception ex) {
			Shared.error("An error occured when player attempted to join the server", ex);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerJoinEvent e) {
		try {
			if (disabled) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) {
				p = new TabPlayer(e.getPlayer());
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				Shared.error("Data of " + p.getName() + " did not exist in JoinEvent. Creating..");
			}
			inject(p);
			p.onJoin();
			final ITabPlayer pl = p;
			Shared.runTask("player joined the server", new Runnable() {


				public void run() {
					me.neznamy.tab.shared.Placeholders.recalculateOnlineVersions();
					HeaderFooter.playerJoin(pl);
					TabObjective.playerJoin(pl);
					NameTag16.playerJoin(pl);
					NameTagX.playerJoin(pl);
					BossBar.playerJoin(pl);
				}
			});
		} catch (Exception ex) {
			Shared.error("An error occured when player joined the server", ex);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void a(final PlayerQuitEvent e){
		if (disabled) return;
		Shared.runTask("player left the server", new Runnable() {


			public void run() {
				ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
				me.neznamy.tab.shared.Placeholders.recalculateOnlineVersions();
				NameTag16.playerQuit(disconnectedPlayer);
				NameTagX.playerQuit(disconnectedPlayer);
				for (ITabPlayer all : Shared.getPlayers()) {
					NameTagLineManager.removeFromRegistered(all, disconnectedPlayer);
				}

				//attepting to fix "floating nametags when players leave"
				//why the fuck is this needed
				int[] ids = new int[disconnectedPlayer.getArmorStands().size()];
				for (int i=0; i<disconnectedPlayer.getArmorStands().size(); i++) {
					ids[i] = disconnectedPlayer.getArmorStands().get(i).getEntityId();
				}
				PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(ids);
				for (ITabPlayer all : Shared.getPlayers()) {
					destroy.send(all);
				}
				//
				Shared.data.remove(e.getPlayer().getUniqueId());
			}
		});
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerChangedWorldEvent e){
		try {
			if (disabled) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) return;
			PerWorldPlayerlist.trigger(e.getPlayer());
			String from = e.getFrom().getName();
			String to = p.getWorldName();
			p.onWorldChange(from, to);
		} catch (Exception ex) {
			Shared.error("An error occured when processing PlayerChangedWorldEvent", ex);
		}
	}
	@EventHandler
	public void a(PlayerCommandPreprocessEvent e) {
		ITabPlayer sender = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (e.getMessage().equalsIgnoreCase("/tab") || e.getMessage().equalsIgnoreCase("/tab:tab")) {
			sendPluginInfo(sender);
			return;
		}
		if (BossBar.onChat(sender, e.getMessage())) e.setCancelled(true);
	}
	public static void inject(ITabPlayer p) {
		Packet.inject(p, new PacketReader() {

			public void onPacketSend(PacketSendEvent e) throws Exception {
				ITabPlayer p = e.getPlayer();
				PacketPlayOut packet = e.getPacket();
				if (packet instanceof PacketPlayOutPlayerInfo) {
					if (Playerlist.modifyPacketOrCancel((PacketPlayOutPlayerInfo) packet, p)) e.setCancelled(true);
				}
				if (packet instanceof PacketPlayOutEntityMetadata) {
					List<Item> items = ((PacketPlayOutEntityMetadata)packet).getList();
					for (Item petOwner : items) {
						if (petOwner.getType().getPosition() == (NMSClass.versionNumber>=14?16:14)) {
							modify(petOwner);
						}
					}
				}
				if (packet instanceof PacketPlayOutSpawnEntityLiving) {
					DataWatcher watcher = ((PacketPlayOutSpawnEntityLiving)packet).getDataWatcher();
					Item petOwner = watcher.getItem(NMSClass.versionNumber>=14?16:14);
					if (petOwner != null) {
						modify(petOwner);
					}
				}
			}
			public void onNameTagXPacket(final PacketSendEvent e) {
				final ITabPlayer p = e.getPlayer();
				if (p.disabledNametag) return;
				//sending packets outside of the packet reader or protocollib will complain
				Shared.runTask("processing packet out", new Runnable() {


					public void run() {
						NameTagX.processPacketOUT(e.getPacket(), p);
					}
				});
			}
		});
	}
	@SuppressWarnings("rawtypes")
	private static void modify(Item petOwner) {
		//1.12-
		if (petOwner.getValue() instanceof com.google.common.base.Optional) {
			com.google.common.base.Optional o = (com.google.common.base.Optional) petOwner.getValue();
			if (o.isPresent() && o.get() instanceof UUID) {
				petOwner.setValue(com.google.common.base.Optional.of(UUID.randomUUID()));
			}
		}
		//1.13+
		if (petOwner.getValue() instanceof java.util.Optional) {
			java.util.Optional o = (java.util.Optional) petOwner.getValue();
			if (o.isPresent() && o.get() instanceof UUID) {
				petOwner.setValue(java.util.Optional.of(UUID.randomUUID()));
			}
		}
	}
	@SuppressWarnings("unchecked")
	public Object createComponent(String text) {
		if (text == null || text.length() == 0) return MethodAPI.getInstance().ICBC_fromString("{\"translate\":\"\"}");
		JSONObject object = new JSONObject();
		object.put("text", text);
		return MethodAPI.getInstance().ICBC_fromString(object.toString());
	}
	public void sendConsoleMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(message);
	}
	public boolean listNames() {
		return Playerlist.enable;
	}
	public String getPermissionPlugin() {
		if (Main.pex) return "PermissionsEx";
		if (Main.groupManager != null) return "GroupManager";
		if (Main.luckPerms) return "LuckPerms";
		if (Main.powerfulPerms != null) return "PowerfulPerms";
		if (Placeholders.perm != null) return Placeholders.perm.getName() + " (detected by Vault)";
		return "-";
	}
	public String getSeparatorType() {
		return "world";
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void reload(ITabPlayer sender) {
		unload();
		load(true, false);
		if (!disabled) TabCommand.sendMessage(sender, Configs.reloaded);
	}
	public void sendPluginInfo(ITabPlayer to) {
		FancyMessage message = new FancyMessage();
		message.add(new Extra("§3TAB v" + Shared.pluginVersion).onHover(HoverAction.SHOW_TEXT, "§aClick to visit plugin's spigot page").onClick(ClickAction.OPEN_URL, "https://www.spigotmc.org/resources/57806/"));
		message.add(new Extra(" §0by _NEZNAMY_ (discord: NEZNAMY#4659)"));
		PacketAPI.sendFancyMessage(to, message);
	}
	@SuppressWarnings("unchecked")
	public boolean killPacket(Object packetPlayOutScoreboardTeam) throws Exception{
		if (PacketPlayOutScoreboardTeam.PacketPlayOutScoreboardTeam_SIGNATURE.getInt(packetPlayOutScoreboardTeam) != 69) {
			Collection<String> players = (Collection<String>) PacketPlayOutScoreboardTeam.PacketPlayOutScoreboardTeam_PLAYERS.get(packetPlayOutScoreboardTeam);
			for (ITabPlayer p : Shared.getPlayers()) {
				if (players.contains(p.getName())) return true;
			}
		}
		return false;
	}
	public Object toNMS(UniversalPacketPlayOut packet, int protocolVersion) throws Exception {
		return packet.toNMS();
	}
}