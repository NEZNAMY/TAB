package me.neznamy.tab.shared.features.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import me.neznamy.tab.api.event.EventHandler;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.feature.*;
import me.neznamy.tab.shared.features.redis.message.*;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Feature synchronizing player display data between
 * multiple proxies connected with a redis plugin.
 */
@SuppressWarnings("UnstableApiUsage")
@Getter
public abstract class RedisSupport extends TabFeature implements JoinListener, QuitListener,
        DisplayNameListener, Loadable, UnLoadable, ServerSwitchListener, LoginPacketListener,
        VanishListener, TabListClearListener {

    @NotNull private final String featureName = "RedisSupport";

    /** Redis players on other proxies by their UUID */
    @NotNull protected final Map<UUID, RedisPlayer> redisPlayers = new ConcurrentHashMap<>();

    /** UUID of this proxy to ignore messages coming from the same proxy */
    @NotNull private final UUID proxy = UUID.randomUUID();

    /** Features this one hooks into */
    @NotNull private final List<RedisFeature> features = new ArrayList<>();
    @Nullable private RedisBelowName redisBelowName;
    @Nullable private RedisYellowNumber redisYellowNumber;
    @Nullable private RedisPlayerList redisPlayerList;
    @Nullable private RedisTeams redisTeams;

    private EventHandler<TabPlaceholderRegisterEvent> eventHandler;
    @NotNull private final Map<String, Supplier<RedisMessage>> messages = new HashMap<>();
    @NotNull private final Map<Class<? extends RedisMessage>, String> classStringMap = new HashMap<>();

    public RedisSupport() {
        registerMessage("load", Load.class, Load::new);
        registerMessage("loadrequest", LoadRequest.class, LoadRequest::new);
        registerMessage("join", PlayerJoin.class, PlayerJoin::new);
        registerMessage("quit", PlayerQuit.class, PlayerQuit::new);
        registerMessage("server", ServerSwitch.class, ServerSwitch::new);
        registerMessage("vanish", UpdateVanishStatus.class, UpdateVanishStatus::new);
    }

    public void updateTabFormat(@NotNull TabPlayer p, @NotNull String format) {
        if (redisPlayerList == null) return; // Plugin still loading
        sendMessage(redisPlayerList.new Update(p.getTablistId(), format));
    }

    public void updateTeam(@NotNull TabPlayer p, @NotNull String teamName, @NotNull String tagPrefix,
                              @NotNull String tagSuffix, @NotNull Scoreboard.NameVisibility nameVisibility) {
        if (redisTeams == null) return; // Plugin still loading
        sendMessage(redisTeams.new Update(p.getTablistId(), teamName, tagPrefix, tagSuffix, nameVisibility));
    }

    public void updateBelowName(@NotNull TabPlayer p, int value, @NotNull String fancyValue) {
        if (redisBelowName == null) return; // Plugin still loading
        sendMessage(redisBelowName.new Update(p.getTablistId(), value, fancyValue));
    }

    public void updateYellowNumber(@NotNull TabPlayer p, int value, String fancyValue) {
        if (redisYellowNumber == null) return; // Plugin still loading
        sendMessage(redisYellowNumber.new Update(p.getTablistId(), value, fancyValue));
    }

    /**
     * Processes incoming redis message
     *
     * @param   msg
     *          json message to process
     */
    public void processMessage(@NotNull String msg) {
        TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.REDIS_BUNGEE_MESSAGE, () -> {
            ByteArrayDataInput in = ByteStreams.newDataInput(Base64.getDecoder().decode(msg));
            String proxy = in.readUTF();
            if (proxy.equals(this.proxy.toString())) return; // Message coming from current proxy
            String action = in.readUTF();
            RedisMessage redisMessage = messages.get(action).get();
            redisMessage.read(in);
            redisMessage.process(this);
        });
    }

    /**
     * Sends message to all proxies
     *
     * @param   message
     *          message to send
     */
    public abstract void sendMessage(@NotNull String message);

    /**
     * Registers event and redis message listeners
     */
    public abstract void register();

    /**
     * Unregisters event and redis message listeners
     */
    public abstract void unregister();

    @Override
    public void load() {
        register();
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.BELOW_NAME)) {
            redisBelowName = new RedisBelowName(this, TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BELOW_NAME));
            features.add(redisBelowName);
        }
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.YELLOW_NUMBER)) {
            redisYellowNumber = new RedisYellowNumber(this, TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.YELLOW_NUMBER));
            features.add(redisYellowNumber);
        }
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PLAYER_LIST)) {
            redisPlayerList = new RedisPlayerList(this, TAB.getInstance().getFeatureManager().getFeature(
                    TabConstants.Feature.PLAYER_LIST));
            features.add(redisPlayerList);
        }
        if (TAB.getInstance().getNameTagManager() != null) {
            redisTeams = new RedisTeams(this, (NameTag) TAB.getInstance().getNameTagManager());
            features.add(redisTeams);
        }
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.GLOBAL_PLAYER_LIST)) {
            features.add(new RedisGlobalPlayerList(this, TAB.getInstance().getFeatureManager().getFeature(
                    TabConstants.Feature.GLOBAL_PLAYER_LIST)));
        }
        eventHandler = event -> {
            String identifier = event.getIdentifier();
            if (identifier.startsWith("%online_")) {
                String server = identifier.substring(8, identifier.length()-1);
                event.setServerPlaceholder(() ->
                        Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(p -> p.getServer().equals(server) && !p.isVanished()).count() +
                                redisPlayers.values().stream().filter(all -> all.getServer().equals(server) && !all.isVanished()).count());

            }
        };
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.ONLINE, 1000, () ->
                Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> !all.isVanished()).count() +
                        redisPlayers.values().stream().filter(all -> !all.isVanished()).count());
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.STAFF_ONLINE, 1000, () ->
                Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> !all.isVanished() && all.hasPermission(TabConstants.Permission.STAFF)).count() +
                        redisPlayers.values().stream().filter(all -> !all.isVanished() && all.isStaff()).count());
        TAB.getInstance().getEventBus().register(TabPlaceholderRegisterEvent.class, eventHandler);
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) onJoin(p);
        sendMessage(new LoadRequest());
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) onQuit(p);
        TAB.getInstance().getEventBus().unregister(eventHandler);
        unregister();
    }

    @Override
    public void onJoin(@NotNull TabPlayer p) {
        sendMessage(new PlayerJoin(this, p));
        features.forEach(f -> f.onJoin(p));
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        sendMessage(new ServerSwitch(p.getTablistId(), to));
        features.forEach(f -> f.onServerSwitch(p));
    }

    @Override
    public void onQuit(@NotNull TabPlayer p) {
        sendMessage(new PlayerQuit(p.getTablistId()));
    }

    @Override
    public IChatBaseComponent onDisplayNameChange(@NotNull TabPlayer packetReceiver, @NotNull UUID id) {
        if (redisPlayerList == null) return null;
        if (!redisPlayerList.getPlayerList().isAntiOverrideTabList()) return null;
        RedisPlayer packetPlayer = redisPlayers.get(id);
        if (packetPlayer != null) {
            return IChatBaseComponent.optimizedComponent(redisPlayerList.getFormat(packetPlayer));
        }
        return null;
    }

    public void sendMessage(@NotNull RedisMessage message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(proxy.toString());
        out.writeUTF(classStringMap.get(message.getClass()));
        message.write(out);
        sendMessage(Base64.getEncoder().encodeToString(out.toByteArray()));
    }

    public void registerMessage(@NotNull String name, @NotNull Class<? extends RedisMessage> clazz, @NotNull Supplier<RedisMessage> supplier) {
        messages.put(name, supplier);
        classStringMap.put(clazz, name);
    }

    @Override
    public void onLoginPacket(TabPlayer player) {
        features.forEach(f -> f.onLoginPacket(player));
    }

    @Override
    public void onTabListClear(TabPlayer player) {
        features.forEach(f -> f.onTabListClear(player));
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        sendMessage(new UpdateVanishStatus(player.getTablistId(), player.isVanished()));
    }
}