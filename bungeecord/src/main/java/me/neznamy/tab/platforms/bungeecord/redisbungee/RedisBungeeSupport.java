package me.neznamy.tab.platforms.bungeecord.redisbungee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.RedisSupport;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.nametags.NameTag;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

@SuppressWarnings("unchecked")
public class RedisBungeeSupport extends TabFeature implements RedisSupport, Listener {

	private static final String CHANNEL_NAME = "TAB";

	private final Map<String, RedisPlayer> redisPlayers = new ConcurrentHashMap<>();
	private final GlobalPlayerList global = (GlobalPlayerList) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST);
	private final PlayerList playerlist = (PlayerList) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);
	private final NameTag nametags = (NameTag) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.NAME_TAGS);
	private final UUID proxy = UUID.randomUUID();

	public RedisBungeeSupport(Plugin plugin) {
		super("RedisBungee", null);
		ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
		RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(CHANNEL_NAME);
		overridePlaceholders();
	}
	
	private void overridePlaceholders() {
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%online%", 2000, p -> {
			int count = 0;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
				if (!all.isVanished() || p.hasPermission(TabConstants.Permission.GLOBAL_PLAYERLIST_SEE_VANISHED)) count++;
			}
			for (RedisPlayer all : redisPlayers.values()){
				if (!all.isVanished() || p.hasPermission(TabConstants.Permission.GLOBAL_PLAYERLIST_SEE_VANISHED)) count++;
			}
			return count;
		});
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%staffonline%", 2000, p -> {
			int count = 0;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
				if (all.hasPermission(TabConstants.Permission.STAFF) && (!all.isVanished() || p.hasPermission(TabConstants.Permission.GLOBAL_PLAYERLIST_SEE_VANISHED))) count++;
			}
			for (RedisPlayer all : redisPlayers.values()){
				if (all.isStaff() && (!all.isVanished() || p.hasPermission(TabConstants.Permission.GLOBAL_PLAYERLIST_SEE_VANISHED))) count++;
			}
			return count;
		});
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			TAB.getInstance().getPlaceholderManager().registerServerPlaceholder("%online_" + server.getKey() + "%", 1000, () -> {
				int count = 0;
				for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
					if (p.getServer().equals(server.getValue().getName()) && !p.isVanished()) count++;
				}
				for (RedisPlayer p : redisPlayers.values()){
					if (p.getServer().equals(server.getValue().getName()) && !p.isVanished()) count++;
				}
				return count;
			});
		}
	}
	
	@Override
	public void load() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) onJoin(p);
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, "{\"action\":\"loadrequest\",\"proxy\":\"" + proxy.toString() + "\"}");
	}

	@Override
	public void unload() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) onQuit(p);
		RedisBungeeAPI.getRedisBungeeApi().unregisterPubSubChannels(CHANNEL_NAME);
		ProxyServer.getInstance().getPluginManager().unregisterListener(this);
	}

	@EventHandler
	public void onMessage(PubSubMessageEvent e) {
		if (!e.getChannel().equals(CHANNEL_NAME)) return;
		TAB.getInstance().getCPUManager().runMeasuredTask("processing PubSubMessageEvent", this, TabConstants.CpuUsageCategory.REDIS_BUNGEE_MESSAGE, () -> {
			JSONObject message;
			try {
				message = (JSONObject) new JSONParser().parse(e.getMessage());
			} catch (ParseException ex) {
				TAB.getInstance().getErrorManager().printError("Failed to parse json message \"" + e.getMessage() + "\"", ex);
				return;
			}
			if (message.get("proxy").equals(proxy.toString())) return; //message coming from current proxy
			String action = (String) message.get("action");
			UUID id = UUID.fromString((String) message.getOrDefault("UUID", proxy.toString()));
			RedisPlayer target;
			switch(action) {
				case "loadrequest":
					JSONObject json = new JSONObject();
					json.put("proxy", proxy.toString());
					json.put("action", "load");
					List<JSONObject> players = new ArrayList<>();
					for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
						players.add(RedisPlayer.toJson(this, all));
					}
					json.put("players", players);
					RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, json.toString());
					break;
				case "load":
					List<JSONObject> players1 = (List<JSONObject>) message.get("players");
					for (JSONObject obj : players1) {
						RedisPlayer p = RedisPlayer.fromJson(this, obj);
						if (!redisPlayers.containsKey(p.getUniqueId().toString())) {
							redisPlayers.put(p.getUniqueId().toString(), p);
							join(p);
						}
					}
					break;
				case "join":
					target = RedisPlayer.fromJson(this, message);
					redisPlayers.put(id.toString(), target);
					join(target);
					break;
				case "server":
					target = redisPlayers.get(id.toString());
					if (target == null) break;
					String server = (String) message.get("server");
					if (global == null) {
						target.setServer(server);
						return;
					}
					for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
						if (viewer.getVersion().getMinorVersion() < 8) continue;
						boolean before = shouldSee(viewer, target.getServer(), target.isVanished());
						boolean after = shouldSee(viewer, server, target.isVanished());
						if (!before && after) {
							viewer.sendCustomPacket(target.getAddPacket(), this);
						}
						if (before && !after) {
							viewer.sendCustomPacket(target.getRemovePacket(), this);
						}
					}
					target.setServer(server);
					break;
				case "tabformat":
					target = redisPlayers.get(id.toString());
					if (target == null) break;
					target.setTabFormat((String) message.get("tabformat"));
					for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
						if (viewer.getVersion().getMinorVersion() >= 8) viewer.sendCustomPacket(target.getUpdatePacket(), this);
					}
					break;
				case "nametag":
					target = redisPlayers.get(id.toString());
					if (target == null) break;
					target.setTagPrefix((String) message.get(TabConstants.Property.TAGPREFIX));
					target.setTagSuffix((String) message.get(TabConstants.Property.TAGSUFFIX));
					for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
						viewer.sendCustomPacket(target.getUpdateTeamPacket(), this);
					}
					break;
				case "belowname":
					target = redisPlayers.get(id.toString());
					if (target == null) break;
					target.setBelowName((String) message.get("belowname"));
					for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
						viewer.sendCustomPacket(target.getBelowNameUpdatePacket(), this);
					}
					break;
				case "yellow-number":
					target = redisPlayers.get(id.toString());
					if (target == null) break;
					target.setYellowNumber((String) message.get("yellow-number"));
					for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
						viewer.sendCustomPacket(target.getYellowNumberUpdatePacket(), this);
					}
					break;
				case "team":
					target = redisPlayers.get(id.toString());
					if (target == null) break;
					PacketPlayOutScoreboardTeam unregister = target.getUnregisterTeamPacket();
					target.setTeamName((String) message.get("to"));
					PacketPlayOutScoreboardTeam register = target.getRegisterTeamPacket();
					for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
						viewer.sendCustomPacket(unregister, this);
						viewer.sendCustomPacket(register, this);
					}
					break;
				case "quit":
					target = redisPlayers.get(id.toString());
					if (target == null) break; //player left current proxy and was unloaded from memory, therefore null check didn't pass
					for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
						all.sendCustomPacket(target.getUnregisterTeamPacket(), this);
						if (all.getVersion().getMinorVersion() < 8) continue;
						all.sendCustomPacket(target.getRemovePacket(), this);
					}
					redisPlayers.remove(id.toString());
					break;
				default:
					break;
			}
		});
	}
	
	private void join(RedisPlayer target) {
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			all.sendCustomPacket(target.getRegisterTeamPacket(), this);
			all.sendCustomPacket(target.getBelowNameUpdatePacket(), this);
			all.sendCustomPacket(target.getYellowNumberUpdatePacket(), this);
			if (all.getVersion().getMinorVersion() < 8) continue;
			if (global == null) {
				if (all.getServer().equals(target.getServer())) all.sendCustomPacket(target.getUpdatePacket(), this);
				continue;
			}
			if (shouldSee(all, target.getServer(), target.isVanished())) {
				if (!all.getServer().equals(target.getServer())) {
					all.sendCustomPacket(target.getAddPacket(), this);
				} else {
					all.sendCustomPacket(target.getUpdatePacket(), this);
				}
			}
		}
	}
	
	private boolean shouldSee(TabPlayer viewer, String server, boolean targetVanished) {
		return shouldSee(viewer, viewer.getServer(), server, targetVanished);
	}
	
	private boolean shouldSee(TabPlayer viewer, String viewerServer, String server, boolean targetVanished) {
		if (targetVanished && !viewer.hasPermission(TabConstants.Permission.GLOBAL_PLAYERLIST_SEE_VANISHED)) return false;
		if (global.getSpyServers().contains(viewerServer)) return true;
		return global.getServerGroup(viewerServer).equals(global.getServerGroup(server));
	}

	@Override
	public void onJoin(TabPlayer p) {
		JSONObject obj = RedisPlayer.toJson(this, p);
		obj.put("proxy", proxy.toString());
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, obj.toString());
		for (RedisPlayer redis : redisPlayers.values()) {
			p.sendCustomPacket(redis.getRegisterTeamPacket(), this);
			p.sendCustomPacket(redis.getBelowNameUpdatePacket(), this);
			p.sendCustomPacket(redis.getYellowNumberUpdatePacket(), this);
			if (global == null) continue;
			if (shouldSee(p, redis.getServer(), redis.isVanished())) {
				if (!p.getServer().equals(redis.getServer())) {
					p.sendCustomPacket(redis.getAddPacket(), this);
				} else {
					p.sendCustomPacket(redis.getUpdatePacket(), this);
				}
			}
		}
	}

	@Override
	public void onServerChange(TabPlayer p, String from, String to) {
		JSONObject json = new JSONObject();
		json.put("proxy", proxy.toString());
		json.put("action", "server");
		json.put("UUID", p.getTablistUUID().toString());
		json.put("server", to);
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, json.toString());
		if (p.getVersion().getMinorVersion() < 8 || global == null) return;
		for (RedisPlayer redis : redisPlayers.values()) {
			boolean before = shouldSee(p, from, redis.getServer(), redis.isVanished());
			boolean after = shouldSee(p, to, redis.getServer(), redis.isVanished());
			if (!before && after) {
				p.sendCustomPacket(redis.getAddPacket(), this);
			}
			if (before && !after) {
				p.sendCustomPacket(redis.getRemovePacket(), this);
			}
		}
	}

	@Override
	public void onQuit(TabPlayer p) {
		JSONObject json = new JSONObject();
		json.put("proxy", proxy.toString());
		json.put("action", "quit");
		json.put("UUID", p.getTablistUUID().toString());
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, json.toString());
	}
	
	@Override
	public void updateTabFormat(TabPlayer p, String format) {
		JSONObject json = new JSONObject();
		json.put("proxy", proxy.toString());
		json.put("action", "tabformat");
		json.put("UUID", p.getTablistUUID().toString());
		json.put("tabformat", format);
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, json.toString());
	}

	@Override
	public void updateNameTag(TabPlayer p, String tagprefix, String tagsuffix) {
		JSONObject json = new JSONObject();
		json.put("proxy", proxy.toString());
		json.put("action", "nametag");
		json.put("UUID", p.getTablistUUID().toString());
		json.put(TabConstants.Property.TAGPREFIX, tagprefix);
		json.put(TabConstants.Property.TAGSUFFIX, tagsuffix);
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, json.toString());
	}
	
	@Override
	public void updateBelowName(TabPlayer p, String value) {
		JSONObject json = new JSONObject();
		json.put("proxy", proxy.toString());
		json.put("action", "belowname");
		json.put("UUID", p.getTablistUUID().toString());
		json.put("belowname", value);
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, json.toString());
	}
	
	@Override
	public void updateYellowNumber(TabPlayer p, String value) {
		JSONObject json = new JSONObject();
		json.put("proxy", proxy.toString());
		json.put("action", "yellow-number");
		json.put("UUID", p.getTablistUUID().toString());
		json.put("yellow-number", value);
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, json.toString());
	}
	
	@Override
	public void updateTeamName(TabPlayer p, String to) {
		JSONObject json = new JSONObject();
		json.put("proxy", proxy.toString());
		json.put("action", "team");
		json.put("UUID", p.getTablistUUID().toString());
		json.put("to", to);
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_NAME, json.toString());
	}
	
	@Override
	public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (info.getAction() == EnumPlayerInfoAction.REMOVE_PLAYER && global != null) {
			for (PlayerInfoData playerInfoData : info.getEntries()) {
				RedisPlayer packetPlayer = redisPlayers.get(playerInfoData.getUniqueId().toString());
				if (packetPlayer != null && (playerInfoData.getName() == null || playerInfoData.getName().length() == 0) && !packetPlayer.isVanished()) {
					//remove packet not coming from tab
					//changing to random non-existing player, the easiest way to cancel the removal
					playerInfoData.setUniqueId(UUID.randomUUID());
				}
			}
		}
		if (info.getAction() != EnumPlayerInfoAction.UPDATE_DISPLAY_NAME && info.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.getEntries()) {
			RedisPlayer packetPlayer = redisPlayers.get(playerInfoData.getUniqueId().toString());
			if (packetPlayer != null && !packetPlayer.hasDisabledPlayerlist()) {
				playerInfoData.setDisplayName(IChatBaseComponent.optimizedComponent(packetPlayer.getTabFormat()));
			}
		}
	}
	
	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		for (RedisPlayer p : redisPlayers.values()) {
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				all.sendCustomPacket(p.getRegisterTeamPacket());
			}
		}
	}
	
	public Map<String, RedisPlayer> getRedisPlayers(){
		return redisPlayers;
	}

	public PlayerList getPlayerlist() {
		return playerlist;
	}

	public NameTag getNametags() {
		return nametags;
	}
}