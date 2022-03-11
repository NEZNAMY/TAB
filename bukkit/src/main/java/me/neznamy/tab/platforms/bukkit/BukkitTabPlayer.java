package me.neznamy.tab.platforms.bukkit;

import java.util.*;

import com.mojang.authlib.properties.Property;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.util.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import com.earth2me.essentials.Essentials;
import com.mojang.authlib.GameProfile;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossFlag;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;

import io.netty.channel.Channel;
import me.libraryaddict.disguise.DisguiseAPI;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * TabPlayer implementation for Bukkit platform
 */
public class BukkitTabPlayer extends ITabPlayer {

    /** Player's NMS handle (EntityPlayer), preloading for speed */
    private Object handle;

    /** Player's connection for sending packets, preloading for speed */
    private Object playerConnection;
    
    /** Bukkit BossBars the player can currently see */
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    /** ViaVersion BossBars this 1.9+ player can see on 1.8 server */
    private final Map<UUID, com.viaversion.viaversion.api.legacy.bossbar.BossBar> viaBossBars = new HashMap<>();

    /**
     * Constructs new instance with given bukkit player and protocol version
     * @param    p
     *             bukkit player
     * @param    protocolVersion
     *             Player's protocol network id
     */
    public BukkitTabPlayer(Player p, int protocolVersion){
        super(p, p.getUniqueId(), p.getName(), "N/A", p.getWorld().getName(), protocolVersion);
        try {
            handle = NMSStorage.getInstance().getHandle.invoke(player);
            playerConnection = NMSStorage.getInstance().PLAYER_CONNECTION.get(handle);
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get playerConnection of " + p.getName(), e);
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        Preconditions.checkNotNull(permission, "permission");
        long time = System.nanoTime();
        boolean value = getPlayer().hasPermission(permission);
        TAB.getInstance().getCPUManager().addMethodTime("hasPermission", System.nanoTime()-time);
        return value;
    }

    @Override
    public int getPing() {
        try {
            int ping = NMSStorage.getInstance().PING.getInt(handle);
            if (ping > 10000 || ping < 0) ping = -1;
            return ping;
        } catch (IllegalAccessException e) {
            return -1;
        }
    }

    @Override
    public void sendPacket(Object nmsPacket) {
        if (nmsPacket == null || !getPlayer().isOnline()) return;
        long time = System.nanoTime();
        try {
            if (nmsPacket instanceof PacketPlayOutBoss) {
                if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
                    handle((PacketPlayOutBoss) nmsPacket);
                } else {
                    handleVia((PacketPlayOutBoss) nmsPacket);
                }
            } else {
                NMSStorage.getInstance().sendPacket.invoke(playerConnection, nmsPacket);
            }
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("An error occurred when sending " + nmsPacket.getClass().getSimpleName(), e);
        }
        TAB.getInstance().getCPUManager().addMethodTime("sendPacket", System.nanoTime()-time);
    }

    /**
     * Handles PacketPlayOutBoss packet send request using Bukkit API,
     * since the API offers everything we need and makes us not need to
     * deal with NMS code at all.
     * @param    packet
     *             packet request to handle using Bukkit API
     */
    private void handle(PacketPlayOutBoss packet) {
        BossBar bar = bossBars.get(packet.getId());
        if (packet.getAction() == PacketPlayOutBoss.Action.ADD) {
            if (bossBars.containsKey(packet.getId())) return;
            bar = Bukkit.createBossBar(RGBUtils.getInstance().convertToBukkitFormat(packet.getName(), getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16),
                    BarColor.valueOf(packet.getColor().name()),
                    BarStyle.valueOf(packet.getOverlay().getBukkitName()));
            if (packet.isCreateWorldFog()) bar.addFlag(BarFlag.CREATE_FOG);
            if (packet.isDarkenScreen()) bar.addFlag(BarFlag.DARKEN_SKY);
            if (packet.isPlayMusic()) bar.addFlag(BarFlag.PLAY_BOSS_MUSIC);
            bar.setProgress(packet.getPct());
            bossBars.put(packet.getId(), bar);
            bar.addPlayer(getPlayer());
            return;
        }
        if (bar == null) return; //no idea how
        switch (packet.getAction()) {
        case REMOVE:
            bar.removePlayer(getPlayer());
            bossBars.remove(packet.getId());
            break;
        case UPDATE_PCT:
            bar.setProgress(packet.getPct());
            break;
        case UPDATE_NAME:
            bar.setTitle(RGBUtils.getInstance().convertToBukkitFormat(packet.getName(), getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16));
            break;
        case UPDATE_STYLE:
            bar.setColor(BarColor.valueOf(packet.getColor().name()));
            bar.setStyle(BarStyle.valueOf(packet.getOverlay().getBukkitName()));
            break;
        case UPDATE_PROPERTIES:
            bar = bossBars.get(packet.getId());
            processFlag(bar, packet.isCreateWorldFog(), BarFlag.CREATE_FOG);
            processFlag(bar, packet.isDarkenScreen(), BarFlag.DARKEN_SKY);
            processFlag(bar, packet.isPlayMusic(), BarFlag.PLAY_BOSS_MUSIC);
            break;
        default:
            break;
        }
    }

    /**
     * Handles PacketPlayOutBoss packet request for 1.9+ clients on
     * 1.8 servers using ViaVersion API instead of using Wither.
     * @param    packet
     *             packet request to handle using ViaVersion API
     */
    private void handleVia(PacketPlayOutBoss packet) {
        com.viaversion.viaversion.api.legacy.bossbar.BossBar bar;
        switch (packet.getAction()) {
        case ADD:
            if (viaBossBars.containsKey(packet.getId())) return;
            bar = Via.getAPI().legacyAPI().createLegacyBossBar(RGBUtils.getInstance().convertToBukkitFormat(packet.getName(), getVersion().getMinorVersion() >= 16), 
                    packet.getPct(),
                    BossColor.valueOf(packet.getColor().name()), 
                    BossStyle.valueOf(packet.getOverlay().getBukkitName()));
            //fog missing from via API
            if (packet.isDarkenScreen()) bar.addFlag(BossFlag.DARKEN_SKY);
            if (packet.isPlayMusic()) bar.addFlag(BossFlag.PLAY_BOSS_MUSIC);
            viaBossBars.put(packet.getId(), bar);
            bar.addPlayer(getPlayer().getUniqueId());
            break;
        case REMOVE:
            viaBossBars.get(packet.getId()).removePlayer(getPlayer().getUniqueId());
            viaBossBars.remove(packet.getId());
            break;
        case UPDATE_PCT:
            viaBossBars.get(packet.getId()).setHealth(packet.getPct());
            break;
        case UPDATE_NAME:
            viaBossBars.get(packet.getId()).setTitle(RGBUtils.getInstance().convertToBukkitFormat(packet.getName(), getVersion().getMinorVersion() >= 16));
            break;
        case UPDATE_STYLE:
            viaBossBars.get(packet.getId()).setColor(BossColor.valueOf(packet.getColor().name()));
            viaBossBars.get(packet.getId()).setStyle(BossStyle.valueOf(packet.getOverlay().getBukkitName()));
            break;
        case UPDATE_PROPERTIES:
            bar = viaBossBars.get(packet.getId());
            //fog missing from via API
            processFlagVia(bar, packet.isDarkenScreen(), BossFlag.DARKEN_SKY);
            processFlagVia(bar, packet.isPlayMusic(), BossFlag.PLAY_BOSS_MUSIC);
            break;
        default:
            break;
        }
    }

    /**
     * Sets BossBar flag to requested target value.
     * @param    bar
     *             BossBar to set flag of
     * @param    targetValue
     *             Target value of the flag
     * @param    flag
     *             Flag to set value of
     */
    private void processFlag(BossBar bar, boolean targetValue, BarFlag flag) {
        if (targetValue && !bar.hasFlag(flag)) bar.addFlag(flag);
        if (!targetValue && bar.hasFlag(flag)) bar.removeFlag(flag);
    }

    /**
     * Sets BossBar flag to requested target value.
     * @param    bar
     *             BossBar to set flag of
     * @param    targetValue
     *             Target value of the flag
     * @param    flag
     *             Flag to set value of
     */
    private void processFlagVia(com.viaversion.viaversion.api.legacy.bossbar.BossBar bar, boolean targetValue, BossFlag flag) {
        if (targetValue && !bar.hasFlag(flag)) bar.addFlag(flag);
        if (!targetValue && bar.hasFlag(flag)) bar.removeFlag(flag);
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override
    public boolean isDisguised() {
        try {
            if (!((BukkitPlatform)TAB.getInstance().getPlatform()).isLibsDisguisesEnabled()) return false;
            return DisguiseAPI.isDisguised(getPlayer());
        } catch (LinkageError e) {
            //java.lang.NoClassDefFoundError: Could not initialize class me.libraryaddict.disguise.DisguiseAPI
            TAB.getInstance().getErrorManager().printError("Failed to check disguise status using LibsDisguises", e);
            ((BukkitPlatform)TAB.getInstance().getPlatform()).setLibsDisguisesEnabled(false);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Skin getSkin() {
        try {
            Collection<Property> col = ((GameProfile)NMSStorage.getInstance().getProfile.invoke(handle)).getProperties().get("textures");
            if (col.isEmpty()) return null; //offline mode
            Property property = col.iterator().next();
            return new Skin(property.getValue(), property.getSignature());
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get skin of " + getName(), e);
            return null;
        }
    }

    @Override
    public Player getPlayer() {
        return (Player) player;
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public boolean isVanished() {
        Essentials essentials = ((BukkitPlatform)TAB.getInstance().getPlatform()).getEssentials();
        if (essentials != null && essentials.getUser(getUniqueId()).isVanished()) return true;
        List<MetadataValue> metadata = getPlayer().getMetadata("vanished");
        return !metadata.isEmpty() && metadata.get(0).asBoolean();
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getGamemode() {
        return getPlayer().getGameMode().getValue();
    }

    @Override
    public Channel getChannel() {
        try {
            if (NMSStorage.getInstance().CHANNEL != null)
                return (Channel) NMSStorage.getInstance().CHANNEL.get(NMSStorage.getInstance().NETWORK_MANAGER.get(playerConnection));
        } catch (IllegalAccessException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get channel of " + getName(), e);
        }
        return null;
    }
}
