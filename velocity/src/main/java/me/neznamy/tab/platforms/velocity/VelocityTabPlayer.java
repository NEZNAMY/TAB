package me.neznamy.tab.platforms.velocity;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import io.netty.channel.Channel;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutChat.ChatMessageType;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.identity.Identity;

/**
 * TabPlayer implementation for Velocity
 */
public class VelocityTabPlayer extends ProxyTabPlayer {

    /**
     * Map of methods executing tasks API calls equal to sending the actual packets
     */
    private final Map<Class<? extends TabPacket>, Consumer<TabPacket>> packetMethods
            = new HashMap<Class<? extends TabPacket>, Consumer<TabPacket>>(){{
        put(PacketPlayOutBoss.class, (packet) -> handle((PacketPlayOutBoss) packet));
        put(PacketPlayOutChat.class, (packet) -> handle((PacketPlayOutChat) packet));
        put(PacketPlayOutPlayerListHeaderFooter.class, (packet) -> handle((PacketPlayOutPlayerListHeaderFooter) packet));
    }};

    /** Player's tablist UUID */
    private final UUID tabListId;
    
    /** BossBars currently displayed to this player */
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    /**
     * Constructs new instance for given player
     *
     * @param   p
     *          velocity player
     */
    public VelocityTabPlayer(Player p) {
        super(p, p.getUniqueId(), p.getUsername(), p.getCurrentServer().isPresent() ?
                p.getCurrentServer().get().getServerInfo().getName() : "-", p.getProtocolVersion().getProtocol());
        UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes(StandardCharsets.UTF_8));
        tabListId = TAB.getInstance().getConfiguration().getConfig().getBoolean("use-online-uuid-in-tablist", true) ? getUniqueId() : offlineId;
        try {
            Object minecraftConnection = player.getClass().getMethod("getConnection").invoke(player);
            channel = (Channel) minecraftConnection.getClass().getMethod("getChannel").invoke(minecraftConnection);
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get channel of " + p.getUsername(), e);
        }
    }

    @Override
    public boolean hasPermission0(String permission) {
        Preconditions.checkNotNull(permission, "permission");
        long time = System.nanoTime();
        boolean value = getPlayer().hasPermission(permission);
        TAB.getInstance().getCPUManager().addMethodTime("hasPermission", System.nanoTime()-time);
        return value;
    }

    @Override
    public int getPing() {
        return (int) getPlayer().getPing();
    }

    @Override
    public void sendPacket(Object packet) {
        long time = System.nanoTime();
        if (packet == null || !getPlayer().isActive()) return;
        if (packetMethods.containsKey(packet.getClass())) {
            packetMethods.get(packet.getClass()).accept((TabPacket) packet);
        } else if (channel != null) {
            channel.writeAndFlush(packet, channel.voidPromise());
        }
        TAB.getInstance().getCPUManager().addMethodTime("sendPacket", System.nanoTime()-time);
    }

    /**
     * Handles PacketPlayOutChat request using Velocity API
     *
     * @param   packet
     *          Packet request to handle
     */
    private void handle(PacketPlayOutChat packet) {
        if (packet.getType() == ChatMessageType.GAME_INFO) {
            getPlayer().sendActionBar(Main.getInstance().convertComponent(packet.getMessage(), getVersion()));
        } else {
            getPlayer().sendMessage(Identity.nil(), Main.getInstance().convertComponent(packet.getMessage(), getVersion()), MessageType.valueOf(packet.getType().name()));
        }
    }

    /**
     * Handles PacketPlayOutPlayerListHeaderFooter request using Velocity API
     *
     * @param   packet
     *          Packet request to handle
     */
    private void handle(PacketPlayOutPlayerListHeaderFooter packet) {
        getPlayer().getTabList().setHeaderAndFooter(Main.getInstance().convertComponent(packet.getHeader(), getVersion()),
                Main.getInstance().convertComponent(packet.getFooter(), getVersion()));
    }

    /**
     * Handles PacketPlayOutBoss request using Velocity API
     *
     * @param   packet
     *          Packet request to handle
     */
    private void handle(PacketPlayOutBoss packet) {
        BossBar bar;
        switch (packet.getAction()) {
        case ADD:
            if (bossBars.containsKey(packet.getId())) return;
            bar = BossBar.bossBar(Main.getInstance().convertComponent(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()),
                    packet.getPct(), 
                    Color.valueOf(packet.getColor().toString()), 
                    Overlay.valueOf(packet.getOverlay().toString()));
            if (packet.isCreateWorldFog()) bar.addFlag(Flag.CREATE_WORLD_FOG);
            if (packet.isDarkenScreen()) bar.addFlag(Flag.DARKEN_SCREEN);
            if (packet.isPlayMusic()) bar.addFlag(Flag.PLAY_BOSS_MUSIC);
            bossBars.put(packet.getId(), bar);
            getPlayer().showBossBar(bar);
            break;
        case REMOVE:
            getPlayer().hideBossBar(bossBars.get(packet.getId()));
            bossBars.remove(packet.getId());
            break;
        case UPDATE_PCT:
            bossBars.get(packet.getId()).progress(packet.getPct());
            break;
        case UPDATE_NAME:
            bossBars.get(packet.getId()).name(Main.getInstance().convertComponent(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()));
            break;
        case UPDATE_STYLE:
            bar = bossBars.get(packet.getId());
            bar.overlay(Overlay.valueOf(packet.getOverlay().toString()));
            bar.color(Color.valueOf(packet.getColor().toString()));
            break;
        case UPDATE_PROPERTIES:
            bar = bossBars.get(packet.getId());
            processFlag(bar, packet.isCreateWorldFog(), Flag.CREATE_WORLD_FOG);
            processFlag(bar, packet.isDarkenScreen(), Flag.DARKEN_SCREEN);
            processFlag(bar, packet.isPlayMusic(), Flag.PLAY_BOSS_MUSIC);
            break;
        default:
            break;
        }
    }

    /**
     * Processes bossbar flag by adding or removing it based on provided value.
     *
     * @param   bar
     *          Bossbar to process flag of
     * @param   targetValue
     *          Flag value
     * @param   flag
     *          Flag to process
     */
    private void processFlag(BossBar bar, boolean targetValue, Flag flag) {
        if (targetValue && !bar.hasFlag(flag)) bar.addFlag(flag);
        if (!targetValue && bar.hasFlag(flag)) bar.removeFlag(flag);
    }

    @Override
    public Skin getSkin() {
        if (getPlayer().getGameProfile().getProperties().size() == 0) return null; //offline mode
        return new Skin(getPlayer().getGameProfile().getProperties().get(0).getValue(), getPlayer().getGameProfile().getProperties().get(0).getSignature());
    }

    @Override
    public Player getPlayer() {
        return (Player) player;
    }

    @Override
    public UUID getTablistUUID() {
        return tabListId;
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isActive();
    }

    @Override
    public int getGamemode() {
        return 0; //shrug
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        Preconditions.checkNotNull(message, "message");
        try {
            Optional<ServerConnection> server = getPlayer().getCurrentServer();
            if (server.isPresent()) {
                server.get().sendPluginMessage(Main.getInstance().getMinecraftChannelIdentifier(), message);
                TAB.getInstance().getCPUManager().packetSent("Plugin Message (" + new String(message) + ")");
            }
        } catch (IllegalStateException e) {
            //java.lang.IllegalStateException: Not connected to server!
        }
    }
}