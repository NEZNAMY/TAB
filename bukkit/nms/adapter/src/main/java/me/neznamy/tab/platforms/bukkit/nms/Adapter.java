package me.neznamy.tab.platforms.bukkit.nms;

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
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public interface Adapter {

    DataWatcherRegistry getDataWatcherRegistry();

    DataWatcher adaptDataWatcher(Object dataWatcher);

    Channel getChannel(Player player);

    int getPing(Player player);

    Object getSkin(Player player);

    void sendPacket(Player player, Object packet);

    IChatBaseComponent adaptComponent(Object component);

    boolean isPlayerInfoPacket(Object packet);

    boolean isTeamPacket(Object packet);

    boolean isDisplayObjectivePacket(Object packet);

    boolean isObjectivePacket(Object packet);

    boolean isInteractPacket(Object packet);

    boolean isMovePacket(Object packet);

    boolean isHeadLookPacket(Object packet);

    boolean isTeleportPacket(Object packet);

    boolean isSpawnLivingEntityPacket(Object packet);

    boolean isSpawnPlayerPacket(Object packet);

    boolean isDestroyPacket(Object packet);

    boolean isMetadataPacket(Object packet);

    boolean isInteractionAction(Object packet);

    Collection<String> getTeamPlayers(Object teamPacket);

    void setTeamPlayers(Object teamPacket, Collection<String> players);

    String getTeamName(Object teamPacket);

    Object createChatPacket(Object component, PacketPlayOutChat.ChatMessageType messageType);

    Object createPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action,
                                  List<PacketPlayOutPlayerInfo.PlayerInfoData> players);

    Object createPlayerListHeaderFooterPacket(IChatBaseComponent header, IChatBaseComponent footer);

    Object createDisplayObjectivePacket(int slot, String objectiveName);

    Object createObjectivePacket(int method, String name, Object displayName,
                                 PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay renderType);

    Object createScorePacket(PacketPlayOutScoreboardScore.Action action, String objectiveName, String player, int score);

    Object createTeamPacket(String name, String prefix, String suffix, String nametagVisibility, String collisionRule,
                            EnumChatFormat color, Collection<String> players, int method, int options);

    Object createEntityDestroyPacket(int[] entities);

    Object createMetadataPacket(int entityId, DataWatcher metadata);

    Object createSpawnLivingEntityPacket(int entityId, UUID uuid, EntityType type, Location location, DataWatcher dataWatcher);

    Object createTeleportPacket(int entityId, Location location);

    PacketPlayOutPlayerInfo createPlayerInfoPacket(Object nmsPacket);

    PacketPlayOutScoreboardObjective createObjectivePacket(Object nmsPacket);

    PacketPlayOutScoreboardDisplayObjective createDisplayObjectivePacket(Object nmsPacket);

    Object adaptComponent(IChatBaseComponent component, ProtocolVersion clientVersion);

    int getMoveEntityId(Object packet);

    int getTeleportEntityId(Object packet);

    int getPlayerSpawnId(Object packet);

    int[] getDestroyEntities(Object packet);

    int getInteractEntityId(Object packet);

    DataWatcher getLivingEntityMetadata(Object packet);

    void setLivingEntityMetadata(Object packet, DataWatcher metadata);

    List<Object> getMetadataEntries(Object packet);

    int getMetadataSlot(Object item);

    Object getMetadataValue(Object item);

    void setInteractEntityId(Object packet, int entityId);
}
