package me.neznamy.tab.shared.features.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import me.neznamy.tab.api.event.EventHandler;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.redis.message.*;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.PerformanceUtil;
import org.jetbrains.annotations.NotNull;

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
        Loadable, UnLoadable, ServerSwitchListener,
        VanishListener {

    /** Redis players on other proxies by their UUID */
    @NotNull protected final Map<UUID, RedisPlayer> redisPlayers = new ConcurrentHashMap<>();

    /** UUID of this proxy to ignore messages coming from the same proxy */
    @NotNull private final UUID proxy = UUID.randomUUID();

    private EventHandler<TabPlaceholderRegisterEvent> eventHandler;
    @NotNull private final Map<String, Supplier<RedisMessage>> messages = new HashMap<>();
    @NotNull private final Map<Class<? extends RedisMessage>, String> classStringMap = new HashMap<>();

    protected RedisSupport() {
        registerMessage("load", Load.class, Load::new);
        registerMessage("loadrequest", LoadRequest.class, LoadRequest::new);
        registerMessage("join", PlayerJoin.class, PlayerJoin::new);
        registerMessage("quit", PlayerQuit.class, PlayerQuit::new);
        registerMessage("server", ServerSwitch.class, ServerSwitch::new);
        registerMessage("vanish", UpdateVanishStatus.class, UpdateVanishStatus::new);
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "RedisSupport";
    }

    /**
     * Processes incoming redis message
     *
     * @param   msg
     *          json message to process
     */
    public void processMessage(@NotNull String msg) {
        // Queue the task to make sure it does not execute before load does, causing NPE
        TAB.getInstance().getCpu().runMeasuredTask(getFeatureName(), CpuUsageCategory.REDIS_BUNGEE_MESSAGE, () -> {
            ByteArrayDataInput in = ByteStreams.newDataInput(Base64.getDecoder().decode(msg));
            String proxy = in.readUTF();
            if (proxy.equals(this.proxy.toString())) return; // Message coming from current proxy
            String action = in.readUTF();
            Supplier<RedisMessage> supplier = messages.get(action);
            if (supplier == null) {
                TAB.getInstance().getErrorManager().unknownRedisMessage(action);
                return;
            }
            RedisMessage redisMessage = supplier.get();
            redisMessage.read(in);
            if (redisMessage.getCustomThread() != null) {
                redisMessage.getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> redisMessage.process(this), getFeatureName(), CpuUsageCategory.REDIS_BUNGEE_MESSAGE));
            } else {
                redisMessage.process(this);
            }
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
        overridePlaceholders();
        TAB.getInstance().getEventBus().register(TabPlaceholderRegisterEvent.class, eventHandler);
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) onJoin(p);
        sendMessage(new LoadRequest());
    }

    private void overridePlaceholders() {
        eventHandler = event -> {
            String identifier = event.getIdentifier();
            if (identifier.startsWith("%online_")) {
                String server = identifier.substring(8, identifier.length()-1);
                event.setServerPlaceholder(() -> {
                    int count = 0;
                    for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                        if (player.server.equals(server) && !player.isVanished()) count++;
                    }
                    for (RedisPlayer player : redisPlayers.values()) {
                        if (player.server.equals(server) && !player.isVanished()) count++;
                    }
                    return PerformanceUtil.toString(count);
                });
            }
        };
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.ONLINE, 1000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.isVanished()) count++;
            }
            for (RedisPlayer player : redisPlayers.values()) {
                if (!player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.STAFF_ONLINE, 1000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.isVanished() && player.hasPermission(TabConstants.Permission.STAFF)) count++;
            }
            for (RedisPlayer player : redisPlayers.values()) {
                if (!player.isVanished() && player.isStaff()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.SERVER_ONLINE, 1000, p -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (((TabPlayer)p).server.equals(player.server) && !player.isVanished()) count++;
            }
            for (RedisPlayer player : redisPlayers.values()) {
                if (((TabPlayer)p).server.equals(player.server) && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) onQuit(p);
        TAB.getInstance().getEventBus().unregister(eventHandler);
        unregister();
    }

    @Override
    public void onJoin(@NotNull TabPlayer p) {
        sendMessage(new PlayerJoin(p));
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        sendMessage(new ServerSwitch(p.getTablistId(), to));
    }

    @Override
    public void onQuit(@NotNull TabPlayer p) {
        sendMessage(new PlayerQuit(p.getTablistId()));
    }

    /**
     * Sends message to other proxies.
     *
     * @param   message
     *          Message to send
     */
    public void sendMessage(@NotNull RedisMessage message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(proxy.toString());
        out.writeUTF(classStringMap.get(message.getClass()));
        message.write(out);
        sendMessage(Base64.getEncoder().encodeToString(out.toByteArray()));
    }

    /**
     * Registers redis message.
     *
     * @param   name
     *          Message name
     * @param   clazz
     *          Message class
     * @param   supplier
     *          Message supplier
     */
    public void registerMessage(@NotNull String name, @NotNull Class<? extends RedisMessage> clazz, @NotNull Supplier<RedisMessage> supplier) {
        messages.put(name, supplier);
        classStringMap.put(clazz, name);
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        sendMessage(new UpdateVanishStatus(player.getTablistId(), player.isVanished()));
    }
}