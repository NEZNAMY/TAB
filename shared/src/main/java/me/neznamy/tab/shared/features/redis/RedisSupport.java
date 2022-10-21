package me.neznamy.tab.shared.features.redis;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature synchronizing player display data between
 * multiple proxies connected with a redis plugin.
 */
@SuppressWarnings("unchecked")
public abstract class RedisSupport extends TabFeature {

    /** Redis players on other proxies by their UUID */
    protected final Map<String, RedisPlayer> redisPlayers = new ConcurrentHashMap<>();

    /** UUID of this proxy to ignore messages coming from the same proxy */
    protected final UUID proxy = UUID.randomUUID();

    /** Global PlayerList feature */
    protected final GlobalPlayerList global = (GlobalPlayerList) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST);

    /** PlayerList feature */
    private final PlayerList playerList = (PlayerList) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);

    /** NameTag feature */
    private final NameTag nameTags = (NameTag) TAB.getInstance().getTeamManager();

    /** Sorting feature */
    private final Sorting sorting = (Sorting) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);

    /**
     * Constructs new instance
     */
    protected RedisSupport() {
        super("RedisBungee", null);
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.ONLINE, 1000, () ->
                Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> !all.isVanished()).count() +
                        redisPlayers.values().stream().filter(all -> !all.isVanished()).count());
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.STAFF_ONLINE, 1000, () ->
                Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> !all.isVanished() && all.hasPermission(TabConstants.Permission.STAFF)).count() +
                        redisPlayers.values().stream().filter(all -> !all.isVanished() && all.isStaff()).count());
    }

    /**
     * Returns all players on other proxies
     *
     * @return  all players on other proxies
     */
    public Map<String, RedisPlayer> getRedisPlayers(){
        return redisPlayers;
    }

    /**
     * Returns PlayerList feature if it's enabled, {@code null} if disabled
     *
     * @return  PlayerList feature instance
     */
    public PlayerList getPlayerList() {
        return playerList;
    }

    /**
     * Returns NameTag feature if it's enabled, {@code null} if disabled
     *
     * @return  NameTag feature instance
     */
    public NameTag getNameTags() {
        return nameTags;
    }

    /**
     * Sends a message to all other proxies to update
     * player list formatting of requested player.
     *
     * @param   p
     *          Player to update
     * @param   format
     *          TabList name format to use
     */
    public void updateTabFormat(TabPlayer p, String format) {
        JSONObject json = new JSONObject();
        json.put("proxy", proxy.toString());
        json.put("action", "tabformat");
        json.put("UUID", p.getTablistUUID().toString());
        json.put("tabformat", format);
        sendMessage(json.toString());
    }

    /**
     * Sends a message to all other proxies to update
     * NameTag prefix / suffix values of requested player.
     *
     * @param   p
     *          Player to update
     * @param   tagPrefix
     *          New NameTag prefix
     * @param   tagSuffix
     *          New NameTag suffix
     */
    public void updateNameTag(TabPlayer p, String tagPrefix, String tagSuffix) {
        JSONObject json = new JSONObject();
        json.put("proxy", proxy.toString());
        json.put("action", "nametag");
        json.put("UUID", p.getTablistUUID().toString());
        json.put(TabConstants.Property.TAGPREFIX, tagPrefix);
        json.put(TabConstants.Property.TAGSUFFIX, tagSuffix);
        sendMessage(json.toString());
    }

    /**
     * Sends a message to all other proxies to update
     * BelowName number of requested player.
     *
     * @param   p
     *          Player to update
     * @param   value
     *          New BelowName value
     */
    public void updateBelowName(TabPlayer p, String value) {
        JSONObject json = new JSONObject();
        json.put("proxy", proxy.toString());
        json.put("action", "belowname");
        json.put("UUID", p.getTablistUUID().toString());
        json.put("belowname", value);
        sendMessage(json.toString());
    }

    /**
     * Sends a message to all other proxies to update
     * yellow number of requested player.
     *
     * @param   p
     *          Player to update
     * @param   value
     *          New number value
     */
    public void updateYellowNumber(TabPlayer p, String value) {
        JSONObject json = new JSONObject();
        json.put("proxy", proxy.toString());
        json.put("action", "yellow-number");
        json.put("UUID", p.getTablistUUID().toString());
        json.put("yellow-number", value);
        sendMessage(json.toString());
    }

    /**
     * Sends a message to all other proxies to change
     * team name of requested player.
     *
     * @param   p
     *          Player to update
     * @param   to
     *          New team name
     */
    public void updateTeamName(TabPlayer p, String to) {
        JSONObject json = new JSONObject();
        json.put("proxy", proxy.toString());
        json.put("action", "team");
        json.put("UUID", p.getTablistUUID().toString());
        json.put("to", to);
        sendMessage(json.toString());
    }

    /**
     * Processes incoming redis message
     *
     * @param   msg
     *          json message to process
     */
    public void processMessage(String msg) {
        TAB.getInstance().getCPUManager().runMeasuredTask(this, TabConstants.CpuUsageCategory.REDIS_BUNGEE_MESSAGE, () -> {
            JSONObject message;
            try {
                message = (JSONObject) new JSONParser().parse(msg);
            } catch (ParseException ex) {
                TAB.getInstance().getErrorManager().printError("Failed to parse json message \"" + msg + "\"", ex);
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
                    sendMessage(json.toString());
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
                        if (!target.getServer().equals(all.getServer())) all.sendCustomPacket(target.getRemovePacket(), this);
                    }
                    redisPlayers.remove(id.toString());
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * Processes player join of specified player and sends packets to everyone
     *
     * @param   target
     *          player to process join of
     */
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
        if (targetVanished && !viewer.hasPermission(TabConstants.Permission.SEE_VANISHED)) return false;
        if (global.getSpyServers().contains(viewerServer)) return true;
        return global.getServerGroup(viewerServer).equals(global.getServerGroup(server));
    }

    /**
     * Sends message to all proxies
     *
     * @param   message
     *          message to send
     */
    public abstract void sendMessage(String message);

    /**
     * Unregisters event and redis message listeners
     */
    public abstract void unregister();


    @Override
    public void load() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) onJoin(p);
        sendMessage("{\"action\":\"loadrequest\",\"proxy\":\"" + proxy.toString() + "\"}");
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) onQuit(p);
        unregister();
    }

    @Override
    public void onJoin(TabPlayer p) {
        JSONObject json = RedisPlayer.toJson(this, p);
        json.put("proxy", proxy.toString());
        sendMessage(json.toString());
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
        sendMessage(json.toString());
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
        sendMessage(json.toString());
    }

    @Override
    public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
        if (info.getAction() == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER && global != null) {
            for (PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData : info.getEntries()) {
                RedisPlayer packetPlayer = redisPlayers.get(playerInfoData.getUniqueId().toString());
                if (packetPlayer != null && (playerInfoData.getName() == null || playerInfoData.getName().length() == 0) && !packetPlayer.isVanished()) {
                    //remove packet not coming from tab
                    //changing to random non-existing player, the easiest way to cancel the removal
                    playerInfoData.setUniqueId(UUID.randomUUID());
                }
            }
        }
        if (info.getAction() != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME && info.getAction() != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER) return;
        for (PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData : info.getEntries()) {
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

    public Sorting getSorting() {
        return sorting;
    }
}