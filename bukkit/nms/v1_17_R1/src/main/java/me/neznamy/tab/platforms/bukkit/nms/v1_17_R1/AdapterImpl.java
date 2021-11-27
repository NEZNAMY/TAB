package me.neznamy.tab.platforms.bukkit.nms.v1_17_R1;

import io.netty.channel.Channel;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.platforms.bukkit.nms.Adapter;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public final class AdapterImpl implements Adapter {

    private final DataWatcherRegistry dataWatcherRegistry = new DataWatcherRegistryImpl();

    @Override
    public DataWatcherRegistry getDataWatcherRegistry() {
        return dataWatcherRegistry;
    }

    @Override
    public DataWatcher adaptDataWatcher(Object dataWatcher) {
        return null;
    }

    @Override
    public Channel getChannel(Player player) {
        return null;
    }

    @Override
    public int getPing(Player player) {
        return 0;
    }

    @Override
    public Object getSkin(Player player) {
        return null;
    }

    @Override
    public void sendPacket(Player player, Object packet) {

    }

    @Override
    public IChatBaseComponent adaptComponent(Object component) {
        return null;
    }

    @Override
    public boolean isPlayerInfoPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isTeamPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isDisplayObjectivePacket(Object packet) {
        return false;
    }

    @Override
    public boolean isObjectivePacket(Object packet) {
        return false;
    }

    @Override
    public boolean isInteractPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isMovePacket(Object packet) {
        return false;
    }

    @Override
    public boolean isHeadLookPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isTeleportPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isSpawnLivingEntityPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isSpawnPlayerPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isDestroyPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isMetadataPacket(Object packet) {
        return false;
    }

    @Override
    public boolean isInteractionAction(Object packet) {
        return false;
    }

    @Override
    public Collection<String> getTeamPlayers(Object teamPacket) {
        return null;
    }

    @Override
    public void setTeamPlayers(Object teamPacket, Collection<String> players) {

    }

    @Override
    public String getTeamName(Object teamPacket) {
        return null;
    }

    @Override
    public Object createChatPacket(Object component, PacketPlayOutChat.ChatMessageType messageType) {
        return null;
    }

    @Override
    public Object createPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, List<PacketPlayOutPlayerInfo.PlayerInfoData> players) {
        return null;
    }

    @Override
    public Object createPlayerListHeaderFooterPacket(IChatBaseComponent header, IChatBaseComponent footer) {
        return null;
    }

    @Override
    public Object createDisplayObjectivePacket(int slot, String objectiveName) {
        return null;
    }

    @Override
    public Object createObjectivePacket(int method, String name, Object displayName, PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay renderType) {
        return null;
    }

    @Override
    public Object createScorePacket(PacketPlayOutScoreboardScore.Action action, String objectiveName, String player, int score) {
        return null;
    }

    @Override
    public Object createTeamPacket(String name, String prefix, String suffix, String nametagVisibility, String collisionRule, EnumChatFormat color, Collection<String> players, int method, int options) {
        return null;
    }

    @Override
    public Object createEntityDestroyPacket(int[] entities) {
        return null;
    }

    @Override
    public Object createMetadataPacket(int entityId, DataWatcher metadata) {
        return null;
    }

    @Override
    public Object createSpawnLivingEntityPacket(int entityId, UUID uuid, EntityType type, Location location, DataWatcher dataWatcher) {
        return null;
    }

    @Override
    public Object createTeleportPacket(int entityId, Location location) {
        return null;
    }

    @Override
    public PacketPlayOutPlayerInfo createPlayerInfoPacket(Object nmsPacket) {
        return null;
    }

    @Override
    public PacketPlayOutScoreboardObjective createObjectivePacket(Object nmsPacket) {
        return null;
    }

    @Override
    public PacketPlayOutScoreboardDisplayObjective createDisplayObjectivePacket(Object nmsPacket) {
        return null;
    }

    @Override
    public Object adaptComponent(IChatBaseComponent component, ProtocolVersion clientVersion) {
        return null;
    }

    @Override
    public int getMoveEntityId(Object packet) {
        return 0;
    }

    @Override
    public int getTeleportEntityId(Object packet) {
        return 0;
    }

    @Override
    public int getPlayerSpawnId(Object packet) {
        return 0;
    }

    @Override
    public int[] getDestroyEntities(Object packet) {
        return new int[0];
    }

    @Override
    public int getInteractEntityId(Object packet) {
        return 0;
    }

    @Override
    public DataWatcher getLivingEntityMetadata(Object packet) {
        return null;
    }

    @Override
    public void setLivingEntityMetadata(Object packet, DataWatcher metadata) {

    }

    @Override
    public List<Object> getMetadataEntries(Object packet) {
        return null;
    }

    @Override
    public int getMetadataSlot(Object item) {
        return 0;
    }

    @Override
    public Object getMetadataValue(Object item) {
        return null;
    }

    @Override
    public void setInteractEntityId(Object packet, int entityId) {

    }
}
