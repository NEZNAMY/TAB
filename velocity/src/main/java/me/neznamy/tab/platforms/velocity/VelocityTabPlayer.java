package me.neznamy.tab.platforms.velocity;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.GameProfile.Property;

import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutChat.ChatMessageType;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

/**
 * TabPlayer for Velocity
 */
public class VelocityTabPlayer extends ProxyTabPlayer {

    private final Map<Class<? extends TabPacket>, Consumer<TabPacket>> packetMethods
            = new HashMap<Class<? extends TabPacket>, Consumer<TabPacket>>(){{
        put(PacketPlayOutBoss.class, (packet) -> handle((PacketPlayOutBoss) packet));
        put(PacketPlayOutChat.class, (packet) -> handle((PacketPlayOutChat) packet));
        put(PacketPlayOutPlayerInfo.class, (packet) -> handle((PacketPlayOutPlayerInfo) packet));
        put(PacketPlayOutPlayerListHeaderFooter.class, (packet) -> handle((PacketPlayOutPlayerListHeaderFooter) packet));
        put(PacketPlayOutScoreboardDisplayObjective.class, (packet) -> handle((PacketPlayOutScoreboardDisplayObjective) packet));
        put(PacketPlayOutScoreboardObjective.class, (packet) -> handle((PacketPlayOutScoreboardObjective) packet));
        put(PacketPlayOutScoreboardScore.class, (packet) -> handle((PacketPlayOutScoreboardScore) packet));
        put(PacketPlayOutScoreboardTeam.class, (packet) -> handle((PacketPlayOutScoreboardTeam) packet));
    }};

    //uuid used in TabList
    private final UUID tabListId;
    
    //player's visible boss bars
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    /**
     * Constructs new instance for given player
     * @param p - velocity player
     */
    public VelocityTabPlayer(Player p) {
        super(p, p.getUniqueId(), p.getUsername(), p.getCurrentServer().isPresent() ?
                p.getCurrentServer().get().getServerInfo().getName() : "-", p.getProtocolVersion().getProtocol());
        UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes(StandardCharsets.UTF_8));
        tabListId = TAB.getInstance().getConfiguration().getConfig().getBoolean("use-online-uuid-in-tablist", true) ? getUniqueId() : offlineId;
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
        packetMethods.get(packet.getClass()).accept((TabPacket) packet);
        TAB.getInstance().getCPUManager().addMethodTime("sendPacket", System.nanoTime()-time);
    }

    private void handle(PacketPlayOutChat packet) {
        if (packet.getType() == ChatMessageType.GAME_INFO) {
            getPlayer().sendActionBar(Main.convertComponent(packet.getMessage(), getVersion()));
        } else {
            getPlayer().sendMessage(Identity.nil(), Main.convertComponent(packet.getMessage(), getVersion()), MessageType.valueOf(packet.getType().name()));
        }
    }
    
    private void handle(PacketPlayOutPlayerListHeaderFooter packet) {
        getPlayer().getTabList().setHeaderAndFooter(Main.convertComponent(packet.getHeader(), getVersion()), Main.convertComponent(packet.getFooter(), getVersion()));
    }

    private void handle(PacketPlayOutPlayerInfo packet) {
        for (PlayerInfoData data : packet.getEntries()) {
            switch (packet.getAction()) {
            case ADD_PLAYER:
                if (getPlayer().getTabList().containsEntry(data.getUniqueId())) continue;
                getPlayer().getTabList().addEntry(TabListEntry.builder()
                        .tabList(getPlayer().getTabList())
                        .displayName(Main.convertComponent(data.getDisplayName(), getVersion()))
                        .gameMode(data.getGameMode().ordinal()-1)
                        .profile(new GameProfile(data.getUniqueId(), data.getName(), data.getSkin() == null ? new ArrayList<>() :
                                Collections.singletonList(new Property("textures", data.getSkin().getValue(), data.getSkin().getSignature()))))
                        .latency(data.getLatency())
                        .build());
                break;
            case REMOVE_PLAYER:
                getPlayer().getTabList().removeEntry(data.getUniqueId());
                break;
            case UPDATE_DISPLAY_NAME:
                getEntry(data.getUniqueId()).setDisplayName(Main.convertComponent(data.getDisplayName(), getVersion()));
                break;
            case UPDATE_LATENCY:
                getEntry(data.getUniqueId()).setLatency(data.getLatency());
                break;
            case UPDATE_GAME_MODE:
                getEntry(data.getUniqueId()).setGameMode(data.getGameMode().ordinal()-1);
                break;
            default:
                break;
            }
        }
    }
    
    private void handle(PacketPlayOutBoss packet) {
        BossBar bar;
        switch (packet.getAction()) {
        case ADD:
            if (bossBars.containsKey(packet.getId())) return;
            bar = BossBar.bossBar(Main.convertComponent(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()),
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
            bossBars.get(packet.getId()).name(Main.convertComponent(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()));
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

    private void handle(PacketPlayOutScoreboardDisplayObjective packet) {
        ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler().sendMessage(this,
                "PacketPlayOutScoreboardDisplayObjective", packet.getSlot(), packet.getObjectiveName());
    }

    private void handle(PacketPlayOutScoreboardObjective packet) {
        List<Object> args = new ArrayList<>();
        args.add("PacketPlayOutScoreboardObjective");
        args.add(packet.getObjectiveName());
        args.add(packet.getAction());
        if (packet.getAction() == 0 || packet.getAction() == 2) {
            args.add(getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder()
                    .cutTo(packet.getDisplayName(), 32) : packet.getDisplayName());
            args.add(IChatBaseComponent.optimizedComponent(packet.getDisplayName()).toString(getVersion()));
            args.add(packet.getRenderType().ordinal());
        }
        ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler().sendMessage(this, args.toArray());
    }

    private void handle(PacketPlayOutScoreboardScore packet) {
        List<Object> args = new ArrayList<>();
        args.add("PacketPlayOutScoreboardScore");
        args.add(packet.getObjectiveName());
        args.add(packet.getAction().ordinal());
        args.add(packet.getPlayer());
        args.add(packet.getScore());
        ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler().sendMessage(this, args.toArray());
    }

    private void handle(PacketPlayOutScoreboardTeam packet) {
        List<Object> args = new ArrayList<>();
        args.add("PacketPlayOutScoreboardTeam");
        args.add(packet.getName());
        args.add(packet.getAction());
        args.add(packet.getPlayers().size());
        args.addAll(packet.getPlayers());
        if (packet.getAction() == 0 || packet.getAction() == 2) {
            String prefix = getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder()
                    .cutTo(packet.getPlayerPrefix(), 16) : packet.getPlayerPrefix();
            String suffix = getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder()
                    .cutTo(packet.getPlayerSuffix(), 16) : packet.getPlayerSuffix();
            args.add(prefix);
            args.add(IChatBaseComponent.optimizedComponent(prefix).toString(getVersion()));
            args.add(suffix);
            args.add(IChatBaseComponent.optimizedComponent(suffix).toString(getVersion()));
            args.add(packet.getOptions());
            args.add(packet.getNameTagVisibility());
            args.add(packet.getCollisionRule());
            args.add((packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(packet.getPlayerPrefix())).ordinal());
        }
        ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler().sendMessage(this, args.toArray());
    }

    private void processFlag(BossBar bar, boolean targetValue, Flag flag) {
        if (targetValue) {
            if (!bar.hasFlag(flag)) {
                bar.addFlag(flag);
            }
        } else {
            if (bar.hasFlag(flag)) {
                bar.removeFlag(flag);
            }
        }
    }
    
    private TabListEntry getEntry(UUID id) {
        for (TabListEntry entry : getPlayer().getTabList().getEntries()) {
            if (entry.getProfile().getId().equals(id)) return entry;
        }
        //return dummy entry to not cause NPE
        //possibly add logging into the future to see when this happens
        return TabListEntry.builder()
                .tabList(getPlayer().getTabList())
                .displayName(Component.text(""))
                .gameMode(0)
                .profile(new GameProfile(UUID.randomUUID(), "empty", new ArrayList<>()))
                .latency(0)
                .build();
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