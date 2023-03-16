package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.GameProfile.Property;
import lombok.NonNull;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.*;
import java.util.function.Consumer;

/**
 * TabPlayer implementation for Velocity
 */
public class VelocityTabPlayer extends ProxyTabPlayer {

    /** Component cache to save CPU when creating components */
    private static final ComponentCache<IChatBaseComponent, Component> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> GsonComponentSerializer.gson().deserialize(component.toString(clientVersion)));
    
    /**
     * Map of methods executing tasks using Velocity API calls equal to sending the actual packets
     */
    private final Map<Class<? extends TabPacket>, Consumer<TabPacket>> packetMethods
            = new HashMap<Class<? extends TabPacket>, Consumer<TabPacket>>() {{
        put(PacketPlayOutPlayerInfo.class, packet -> handle((PacketPlayOutPlayerInfo) packet));
    }};

    /** BossBars currently displayed to this player */
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    /**
     * Constructs new instance for given player
     *
     * @param   p
     *          velocity player
     */
    public VelocityTabPlayer(Player p) {
        super(p, p.getUniqueId(), p.getUsername(), p.getCurrentServer().get().getServerInfo().getName(), p.getProtocolVersion().getProtocol());
    }
    
    @Override
    public boolean hasPermission0(String permission) {
        return getPlayer().hasPermission(permission);
    }
    
    @Override
    public int getPing() {
        return (int) getPlayer().getPing();
    }
    
    @Override
    public void sendPacket(Object packet) {
        if (packet == null || !getPlayer().isActive()) return;
        packetMethods.get(packet.getClass()).accept((TabPacket) packet);
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendMessage(componentCache.get(message, getVersion()));
    }

    /**
     * Handles PacketPlayOutPlayerInfo request using Velocity API
     *
     * @param   packet
     *          Packet request to handle
     */
    private void handle(PacketPlayOutPlayerInfo packet) {
        for (PlayerInfoData data : packet.getEntries()) {
            for (PacketPlayOutPlayerInfo.EnumPlayerInfoAction action : packet.getActions()) {
                switch (action) {
                    case ADD_PLAYER:
                        if (getPlayer().getTabList().containsEntry(data.getUniqueId())) continue;
                        getPlayer().getTabList().addEntry(TabListEntry.builder()
                                .tabList(getPlayer().getTabList())
                                .displayName(componentCache.get(data.getDisplayName(), getVersion()))
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
                        getEntry(data.getUniqueId()).setDisplayName(componentCache.get(data.getDisplayName(), getVersion()));
                        break;
                    case UPDATE_LATENCY:
                        getEntry(data.getUniqueId()).setLatency(data.getLatency());
                        break;
                    case UPDATE_GAME_MODE:
                        getEntry(data.getUniqueId()).setGameMode(data.getGameMode().ordinal()-1);
                        break;
                    case UPDATE_LISTED:
                        try {
                            // 3.1.2+
                            getEntry(data.getUniqueId()).setListed(data.isListed());
                        } catch (NoSuchMethodError e) {
                            // 3.1.1-
                        }
                        break;
                    case INITIALIZE_CHAT: // not supported by Velocity
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Returns TabList entry with specified UUID. If no such entry was found,
     * a new, dummy entry is returned to avoid NPE.
     *
     * @param   id
     *          UUID to get entry by
     * @return  TabList entry with specified UUID
     */
    private TabListEntry getEntry(UUID id) {
        for (TabListEntry entry : getPlayer().getTabList().getEntries()) {
            if (entry.getProfile().getId().equals(id)) return entry;
        }
        //return dummy entry to not cause NPE
        //possibly add logging into the future to see when this happens
        return TabListEntry.builder()
                .tabList(getPlayer().getTabList())
                .displayName(Component.empty())
                .gameMode(0)
                .profile(new GameProfile(id, "", Collections.emptyList()))
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
    public boolean isOnline() {
        return getPlayer().isActive();
    }

    @Override
    public int getGamemode() {
        return getEntry(getTablistId()).getGameMode();
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        getPlayer().sendPlayerListHeaderAndFooter(componentCache.get(header, version), componentCache.get(footer, version));
    }

    @Override
    public void sendBossBar(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        if (bossBars.containsKey(id)) return;
        BossBar bar = BossBar.bossBar(componentCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()),
                progress, Color.valueOf(color.toString()), Overlay.valueOf(style.toString()));
        bossBars.put(id, bar);
        getPlayer().showBossBar(bar);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull String title) {
        bossBars.get(id).name(componentCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()));
    }

    @Override
    public void updateBossBar(@NonNull UUID id, float progress) {
        bossBars.get(id).progress(progress);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarStyle style) {
        bossBars.get(id).overlay(Overlay.valueOf(style.toString()));
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarColor color) {
        bossBars.get(id).color(Color.valueOf(color.toString()));
    }

    @Override
    public void removeBossBar(@NonNull UUID id) {
        getPlayer().hideBossBar(bossBars.remove(id));
    }

    @Override
    public void setObjectiveDisplaySlot(int slot, @NonNull String objective) {
        sendPluginMessage("PacketPlayOutScoreboardDisplayObjective", slot, objective);
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        sendPluginMessage("PacketPlayOutScoreboardObjective", objectiveName, 0,
                getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder().cutTo(title, 32) : title,
                IChatBaseComponent.optimizedComponent(title).toString(getVersion()), hearts ? 1 : 0);
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        sendPluginMessage("PacketPlayOutScoreboardObjective", objectiveName, 1);
    }

    @Override
    public void updateObjectiveTitle0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        sendPluginMessage("PacketPlayOutScoreboardObjective", objectiveName, 2,
                getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder().cutTo(title, 32) : title,
                IChatBaseComponent.optimizedComponent(title).toString(getVersion()), hearts ? 1 : 0);
    }

    @Override
    public void registerScoreboardTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        List<Object> args = new ArrayList<>();
        args.add("PacketPlayOutScoreboardTeam");
        args.add(name);
        args.add(0);
        args.add(players.size());
        args.addAll(players);
        String finalPrefix = getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder()
                .cutTo(prefix, 16) : prefix;
        String finalSuffix = getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder()
                .cutTo(suffix, 16) : suffix;
        args.add(finalPrefix);
        args.add(IChatBaseComponent.optimizedComponent(finalPrefix).toString(getVersion()));
        args.add(finalSuffix);
        args.add(IChatBaseComponent.optimizedComponent(finalSuffix).toString(getVersion()));
        args.add(options);
        args.add(visibility);
        args.add(collision);
        args.add(EnumChatFormat.lastColorsOf(prefix).ordinal());
        sendPluginMessage(args.toArray());
    }

    @Override
    public void unregisterScoreboardTeam0(@NonNull String name) {
        sendPluginMessage("PacketPlayOutScoreboardTeam", name, 1, 0);
    }

    @Override
    public void updateScoreboardTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options) {
        List<Object> args = new ArrayList<>();
        args.add("PacketPlayOutScoreboardTeam");
        args.add(name);
        args.add(2);
        args.add(0);
        String finalPrefix = getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder()
                .cutTo(prefix, 16) : prefix;
        String finalSuffix = getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder()
                .cutTo(suffix, 16) : suffix;
        args.add(finalPrefix);
        args.add(IChatBaseComponent.optimizedComponent(finalPrefix).toString(getVersion()));
        args.add(finalSuffix);
        args.add(IChatBaseComponent.optimizedComponent(finalSuffix).toString(getVersion()));
        args.add(options);
        args.add(visibility);
        args.add(collision);
        args.add(EnumChatFormat.lastColorsOf(prefix).ordinal());
        sendPluginMessage(args.toArray());
    }

    @Override
    public void setScoreboardScore0(@NonNull String objective, @NonNull String player, int score) {
        sendPluginMessage("PacketPlayOutScoreboardScore", objective, 0, player, score);
    }

    @Override
    public void removeScoreboardScore0(@NonNull String objective, @NonNull String player) {
        sendPluginMessage("PacketPlayOutScoreboardScore", objective, 1, player, 0);
    }

    @Override
    public Object getProfilePublicKey() {
        try {
            // 3.1.2+
            return getPlayer().getIdentifiedKey();
        } catch (NoSuchMethodError e) {
            // 3.1.1-
            return null;
        }
    }

    @Override
    public UUID getChatSessionId() {
        return null; // not supported on velocity
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        try {
            getPlayer().getCurrentServer().ifPresent(server -> server.sendPluginMessage(VelocityTAB.getMinecraftChannelIdentifier(), message));
        } catch (IllegalStateException VelocityBeingVelocityException) {
            // java.lang.IllegalStateException: Not connected to server!
        }
    }
}