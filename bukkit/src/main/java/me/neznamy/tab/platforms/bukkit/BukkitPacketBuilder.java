package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.WrappedChatComponent;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.storage.NMSStorage;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitPacketBuilder extends PacketBuilder {

    /** NMS data storage */
    private final NMSStorage nms = NMSStorage.getInstance();

    /** Component cache for better performance (1.16+ players) */

    private final ComponentCache<IChatBaseComponent, Object> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> nms.ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));

    /**
     * Constructs new instance
     */
    public BukkitPacketBuilder() {
        buildMap.put(PacketPlayOutEntityMetadata.class, (packet, version) -> ((PacketPlayOutEntityMetadata)packet).build());
        buildMap.put(PacketPlayOutEntityTeleport.class, (packet, version) -> ((PacketPlayOutEntityTeleport)packet).build());
        buildMap.put(PacketPlayOutEntityDestroy.class, (packet, version) -> ((PacketPlayOutEntityDestroy)packet).build());
        buildMap.put(PacketPlayOutSpawnEntityLiving.class, (packet, version) -> ((PacketPlayOutSpawnEntityLiving)packet).build());
    }

    @Override
    public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        if (nms.getMinorVersion() >= 9 || clientVersion.getMinorVersion() >= 9) {
            //1.9+ server or client, handled by bukkit api or ViaVersion
            return packet;
        }
        //<1.9 client and server
        return buildBossPacketEntity(packet, clientVersion);
    }

    @Override
    public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        Object component = toNMSComponent(packet.getMessage(), clientVersion);
        if (nms.getMinorVersion() >= 19)
            try {
                return nms.newPacketPlayOutChat.newInstance(component, packet.getType() == PacketPlayOutChat.ChatMessageType.GAME_INFO);
            } catch (Exception e) {
                //1.19.0
                return nms.newPacketPlayOutChat.newInstance(component, packet.getType().ordinal());
            }
        if (nms.getMinorVersion() >= 16)
            return nms.newPacketPlayOutChat.newInstance(component, Enum.valueOf(nms.ChatMessageType, packet.getType().toString()), UUID.randomUUID());
        if (nms.getMinorVersion() >= 12)
            return nms.newPacketPlayOutChat.newInstance(component, Enum.valueOf(nms.ChatMessageType, packet.getType().toString()));
        if (nms.getMinorVersion() >= 8)
            return nms.newPacketPlayOutChat.newInstance(component, (byte) packet.getType().ordinal());
        if (nms.getMinorVersion() == 7)
            return nms.newPacketPlayOutChat.newInstance(component);
        return packet;
    }

    @Override
    public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        if (nms.getMinorVersion() < 8) return null;
        if (nms.ClientboundPlayerInfoRemovePacket != null) {
            //1.19.3+
            if (packet.getActions().contains(EnumPlayerInfoAction.REMOVE_PLAYER)) {
                return nms.newClientboundPlayerInfoRemovePacket.newInstance(
                        packet.getEntries().stream().map(PlayerInfoData::getUniqueId).collect(Collectors.toList())
                );
            }
            Enum[] array = packet.getActions().stream().map(action -> Enum.valueOf(nms.EnumPlayerInfoAction, action.toString())).toArray(Enum[]::new);
            Object nmsPacket = nms.newPacketPlayOutPlayerInfo.newInstance(EnumSet.of(array[0], array), Collections.emptyList());
            List<Object> items = new ArrayList<>();
            for (PlayerInfoData data : packet.getEntries()) {
                GameProfile profile = new GameProfile(data.getUniqueId(), data.getName());
                if (data.getSkin() != null) profile.getProperties().put("textures",
                        new Property("textures", data.getSkin().getValue(), data.getSkin().getSignature()));
                Object obj = nms.newPlayerInfoData.newInstance(
                        data.getUniqueId(),
                        profile,
                        data.isListed(),
                        data.getLatency(),
                        data.getGameMode() == null ? null : Enum.valueOf(nms.EnumGamemode, data.getGameMode().toString()),
                        data.getDisplayName() == null ? null : toNMSComponent(data.getDisplayName(), clientVersion),
                        data.getProfilePublicKey() == null ? null : nms.newRemoteChatSession$Data.newInstance(data.getChatSessionId(), data.getProfilePublicKey()));
                items.add(obj);
            }
            nms.setField(nmsPacket, nms.PacketPlayOutPlayerInfo_PLAYERS, items);
            return nmsPacket;
        } else {
            //1.19.2-
            EnumPlayerInfoAction action = packet.getActions().contains(EnumPlayerInfoAction.ADD_PLAYER) ?
                    EnumPlayerInfoAction.ADD_PLAYER : packet.getActions().iterator().next();
            Object nmsPacket = nms.newPacketPlayOutPlayerInfo.newInstance(Enum.valueOf(nms.EnumPlayerInfoAction, action.toString()), Array.newInstance(nms.EntityPlayer, 0));
            List<Object> items = new ArrayList<>();
            for (PlayerInfoData data : packet.getEntries()) {
                GameProfile profile = new GameProfile(data.getUniqueId(), data.getName());
                if (data.getSkin() != null) profile.getProperties().put("textures", new Property("textures", data.getSkin().getValue(), data.getSkin().getSignature()));
                List<Object> parameters = new ArrayList<>();
                if (nms.newPlayerInfoData.getParameterTypes()[0] == nms.PacketPlayOutPlayerInfo) {
                    parameters.add(nmsPacket);
                }
                parameters.add(profile);
                parameters.add(data.getLatency());
                parameters.add(data.getGameMode() == null ? null : Enum.valueOf(nms.EnumGamemode, data.getGameMode().toString()));
                parameters.add(data.getDisplayName() == null ? null : toNMSComponent(data.getDisplayName(), clientVersion));
                if (nms.getMinorVersion() >= 19) parameters.add(data.getProfilePublicKey());
                items.add(nms.newPlayerInfoData.newInstance(parameters.toArray()));
            }
            nms.setField(nmsPacket, nms.PacketPlayOutPlayerInfo_PLAYERS, items);
            return nmsPacket;
        }
    }

    @Override
    public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        if (nms.getMinorVersion() < 8) return null;
        if (nms.newPacketPlayOutPlayerListHeaderFooter.getParameterCount() == 2) {
            return nms.newPacketPlayOutPlayerListHeaderFooter.newInstance(toNMSComponent(packet.getHeader(), clientVersion), toNMSComponent(packet.getFooter(), clientVersion));
        }
        Object nmsPacket = nms.newPacketPlayOutPlayerListHeaderFooter.newInstance();
        nms.setField(nmsPacket, nms.PacketPlayOutPlayerListHeaderFooter_HEADER, toNMSComponent(packet.getHeader(), clientVersion));
        nms.setField(nmsPacket, nms.PacketPlayOutPlayerListHeaderFooter_FOOTER, toNMSComponent(packet.getFooter(), clientVersion));
        return nmsPacket;
    }

    @Override
    public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        return nms.newPacketPlayOutScoreboardDisplayObjective.newInstance(packet.getSlot(), newScoreboardObjective(packet.getObjectiveName()));
    }

    @Override
    public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        String displayName = clientVersion.getMinorVersion() < 13 ? cutTo(packet.getDisplayName(), 32) : packet.getDisplayName();
        if (nms.getMinorVersion() >= 13) {
            return nms.newPacketPlayOutScoreboardObjective.newInstance(nms.newScoreboardObjective.newInstance(null, packet.getObjectiveName(), null, 
                    toNMSComponent(IChatBaseComponent.optimizedComponent(displayName), clientVersion), 
                    packet.getRenderType() == null ? null : Enum.valueOf(nms.EnumScoreboardHealthDisplay, packet.getRenderType().toString())),
                    packet.getAction());
        }

        Object nmsPacket = nms.newPacketPlayOutScoreboardObjective.newInstance();
        nms.setField(nmsPacket, nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME, packet.getObjectiveName());
        nms.setField(nmsPacket, nms.PacketPlayOutScoreboardObjective_DISPLAYNAME, displayName);
        if (nms.getMinorVersion() >= 8 && packet.getRenderType() != null) {
            nms.setField(nmsPacket, nms.PacketPlayOutScoreboardObjective_RENDERTYPE, Enum.valueOf(nms.EnumScoreboardHealthDisplay, packet.getRenderType().toString()));
        }
        nms.setField(nmsPacket, nms.PacketPlayOutScoreboardObjective_METHOD, packet.getAction());
        return nmsPacket;
    }

    @Override
    public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        if (nms.getMinorVersion() >= 13) {
            return nms.newPacketPlayOutScoreboardScore_1_13.newInstance(Enum.valueOf(nms.EnumScoreboardAction, packet.getAction().toString()), packet.getObjectiveName(), packet.getPlayer(), packet.getScore());
        }
        if (packet.getAction() == PacketPlayOutScoreboardScore.Action.REMOVE) {
            return nms.newPacketPlayOutScoreboardScore_String.newInstance(packet.getPlayer());
        }
        Object score = nms.newScoreboardScore.newInstance(nms.emptyScoreboard, newScoreboardObjective(packet.getObjectiveName()), packet.getPlayer());
        nms.ScoreboardScore_setScore.invoke(score, packet.getScore());
        if (nms.getMinorVersion() >= 8) {
            return nms.newPacketPlayOutScoreboardScore.newInstance(score);
        }
        return nms.newPacketPlayOutScoreboardScore.newInstance(score, 0);
    }

    @Override
    public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        if (nms.PacketPlayOutScoreboardTeam == null) return null; //fabric
        Object team = nms.newScoreboardTeam.newInstance(nms.emptyScoreboard, packet.getName());
        String prefix = packet.getPlayerPrefix();
        String suffix = packet.getPlayerSuffix();
        if (clientVersion.getMinorVersion() < 13) {
            prefix = cutTo(prefix, 16);
            suffix = cutTo(suffix, 16);
        }
        ((Collection<String>)nms.ScoreboardTeam_getPlayerNameSet.invoke(team)).addAll(packet.getPlayers());
        nms.ScoreboardTeam_setAllowFriendlyFire.invoke(team, (packet.getOptions() & 0x1) > 0);
        nms.ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(team, (packet.getOptions() & 0x2) > 0);
        if (nms.getMinorVersion() >= 13) {
            createTeamModern(packet, clientVersion, team, prefix, suffix);
        } else {
            createTeamLegacy(packet, team, prefix, suffix);
        }
        if (nms.getMinorVersion() >= 17) {
            switch (packet.getAction()) {
            case 0:
                return nms.PacketPlayOutScoreboardTeam_ofBoolean.invoke(null, team, true);
            case 1:
                return nms.PacketPlayOutScoreboardTeam_of.invoke(null, team);
            case 2:
                return nms.PacketPlayOutScoreboardTeam_ofBoolean.invoke(null, team, false);
            case 3:
                return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, packet.getPlayers().iterator().next(), Enum.valueOf(nms.PacketPlayOutScoreboardTeam_PlayerAction, "ADD"));
            case 4:
                return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, packet.getPlayers().iterator().next(), Enum.valueOf(nms.PacketPlayOutScoreboardTeam_PlayerAction, "REMOVE"));
            default:
                throw new IllegalArgumentException("Invalid action: " + packet.getAction());
            }
        }
        return nms.newPacketPlayOutScoreboardTeam.newInstance(team, packet.getAction());
    }

    /**
     * Writes data into NMS team from custom team packet. Used on 1.13+ servers.
     *
     * @param   packet
     *          Packet to read from
     * @param   clientVersion
     *          Version of player
     * @param   team
     *          Team to write to
     * @param   prefix
     *          Prefix to use
     * @param   suffix
     *          Suffix to use
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    private void createTeamModern(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion, Object team, String prefix, String suffix) throws ReflectiveOperationException {
        if (prefix != null) nms.ScoreboardTeam_setPrefix.invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(prefix), clientVersion));
        if (suffix != null) nms.ScoreboardTeam_setSuffix.invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(suffix), clientVersion));
        EnumChatFormat format = packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(prefix);
        nms.ScoreboardTeam_setColor.invoke(team, Enum.valueOf(nms.EnumChatFormat, format.toString()));
        nms.ScoreboardTeam_setNameTagVisibility.invoke(team, Enum.valueOf(nms.EnumNameTagVisibility, String.valueOf(packet.getNameTagVisibility()).equals("always") ? "ALWAYS" : "NEVER"));
        nms.ScoreboardTeam_setCollisionRule.invoke(team, Enum.valueOf(nms.EnumTeamPush, String.valueOf(packet.getCollisionRule()).equals("always") ? "ALWAYS" : "NEVER"));
    }

    /**
     * Writes data into NMS team from custom team packet. Used on 1.12- servers.
     *
     * @param   packet
     *          Packet to read from
     * @param   team
     *          Team to write to
     * @param   prefix
     *          Prefix to use
     * @param   suffix
     *          Suffix to use
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    private void createTeamLegacy(PacketPlayOutScoreboardTeam packet, Object team, String prefix, String suffix) throws ReflectiveOperationException {
        if (prefix != null) nms.ScoreboardTeam_setPrefix.invoke(team, prefix);
        if (suffix != null) nms.ScoreboardTeam_setSuffix.invoke(team, suffix);
        if (nms.getMinorVersion() >= 8) nms.ScoreboardTeam_setNameTagVisibility.invoke(team, Enum.valueOf(nms.EnumNameTagVisibility, String.valueOf(packet.getNameTagVisibility()).equals("always") ? "ALWAYS" : "NEVER"));
        if (nms.getMinorVersion() >= 9) nms.ScoreboardTeam_setCollisionRule.invoke(team, Enum.valueOf(nms.EnumTeamPush, String.valueOf(packet.getCollisionRule()).equals("always") ? "ALWAYS" : "NEVER"));
    }
    
    @Override
    public PacketPlayOutPlayerInfo readPlayerInfo(Object nmsPacket, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        if (nms.getMinorVersion() < 8) return null;
        if (nms.ClientboundPlayerInfoRemovePacket != null) {
            //1.19.3+
            if (nms.ClientboundPlayerInfoRemovePacket.isInstance(nmsPacket)) {
                List<UUID> entries = (List<UUID>) nms.ClientboundPlayerInfoRemovePacket_getEntries.invoke(nmsPacket);
                return new PacketPlayOutPlayerInfo(
                        EnumPlayerInfoAction.REMOVE_PLAYER,
                        entries.stream().map(PlayerInfoData::new).collect(Collectors.toList())
                );
            }
            EnumSet<?> set = (EnumSet<?>) nms.PacketPlayOutPlayerInfo_ACTION.get(nmsPacket);
            EnumPlayerInfoAction[] array = set.stream().map(action -> EnumPlayerInfoAction.valueOf(action.toString())).toArray(EnumPlayerInfoAction[]::new);
            EnumSet<EnumPlayerInfoAction> actions = EnumSet.of(array[0], array);
            List<PlayerInfoData> listData = new ArrayList<>();
            for (Object nmsData : (List<?>) nms.PacketPlayOutPlayerInfo_PLAYERS.get(nmsPacket)) {
                Object nmsGameMode = nms.PlayerInfoData_getGamemode.invoke(nmsData);
                EnumGamemode gameMode = (nmsGameMode == null) ? null : EnumGamemode.valueOf(nmsGameMode.toString());
                GameProfile profile = (GameProfile) nms.PlayerInfoData_getProfile.invoke(nmsData);
                Object nmsComponent = nms.PlayerInfoData_getDisplayName.invoke(nmsData);
                Skin skin = null;
                if (!profile.getProperties().get("textures").isEmpty()) {
                    Property pr = profile.getProperties().get("textures").iterator().next();
                    skin = new Skin(pr.getValue(), pr.getSignature());
                }
                Object remoteChatSession = nms.PlayerInfoData_getProfilePublicKeyRecord.invoke(nmsData);
                listData.add(
                        new PlayerInfoData(
                                profile.getName(),
                                profile.getId(),
                                skin,
                                (boolean) nms.PlayerInfoData_isListed.invoke(nmsData),
                                (int) nms.PlayerInfoData_getLatency.invoke(nmsData),
                                gameMode,
                                nmsComponent == null ? null : new WrappedChatComponent(nmsComponent),
                                remoteChatSession == null ? null : (UUID) nms.RemoteChatSession$Data_getSessionId.invoke(remoteChatSession),
                                remoteChatSession == null ? null : nms.RemoteChatSession$Data_getProfilePublicKey.invoke(remoteChatSession)));
            }
            return new PacketPlayOutPlayerInfo(actions, listData);
        } else {
            //1.19.2-
            EnumPlayerInfoAction action = EnumPlayerInfoAction.valueOf(nms.PacketPlayOutPlayerInfo_ACTION.get(nmsPacket).toString());
            List<PlayerInfoData> listData = new ArrayList<>();
            for (Object nmsData : (List<?>) nms.PacketPlayOutPlayerInfo_PLAYERS.get(nmsPacket)) {
                Object nmsGameMode = nms.PlayerInfoData_getGamemode.invoke(nmsData);
                EnumGamemode gameMode = (nmsGameMode == null) ? null : EnumGamemode.valueOf(nmsGameMode.toString());
                GameProfile profile = (GameProfile) nms.PlayerInfoData_getProfile.invoke(nmsData);
                Object nmsComponent = nms.PlayerInfoData_getDisplayName.invoke(nmsData);
                IChatBaseComponent listName = nmsComponent == null ? null : new WrappedChatComponent(nmsComponent);
                Skin skin = null;
                if (!profile.getProperties().get("textures").isEmpty()) {
                    Property pr = profile.getProperties().get("textures").iterator().next();
                    skin = new Skin(pr.getValue(), pr.getSignature());
                }
                Object profilePublicKey = nms.getMinorVersion() >= 19 ? nms.PlayerInfoData_getProfilePublicKeyRecord.invoke(nmsData) : null;
                listData.add(new PlayerInfoData(profile.getName(), profile.getId(), skin, true,
                        (int) nms.PlayerInfoData_getLatency.invoke(nmsData), gameMode, listName, null, profilePublicKey));
            }
            return new PacketPlayOutPlayerInfo(action, listData);
        }
    }

    @Override
    public PacketPlayOutScoreboardObjective readObjective(Object nmsPacket) throws ReflectiveOperationException {
        return new PacketPlayOutScoreboardObjective(nms.PacketPlayOutScoreboardObjective_METHOD.getInt(nmsPacket),
                (String) nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME.get(nmsPacket), null,
                PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay.INTEGER
        );
    }

    @Override
    public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object nmsPacket) throws ReflectiveOperationException {
        return new PacketPlayOutScoreboardDisplayObjective(
            nms.PacketPlayOutScoreboardDisplayObjective_POSITION.getInt(nmsPacket),
            (String) nms.PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME.get(nmsPacket)
        );
    }

    /**
     * Builds entity packet representing requested BossBar packet using Wither on 1.8- clients.
     *
     * @param   packet
     *          packet to build
     * @param   clientVersion
     *          client version
     * @return  entity BossBar packet
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    private Object buildBossPacketEntity(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        if (packet.getAction() == Action.UPDATE_STYLE) return null; //nothing to do here

        int entityId = packet.getId().hashCode();
        if (packet.getAction() == Action.REMOVE) {
            return new PacketPlayOutEntityDestroy(entityId).build();
        }
        DataWatcher w = new DataWatcher();
        if (packet.getAction() == Action.UPDATE_PCT || packet.getAction() == Action.ADD) {
            float health = 300*packet.getPct();
            if (health == 0) health = 1;
            w.getHelper().setHealth(health);
        }
        if (packet.getAction() == Action.UPDATE_NAME || packet.getAction() == Action.ADD) {
            w.getHelper().setCustomName(packet.getName(), clientVersion);
        }
        if (packet.getAction() == Action.ADD) {
            w.getHelper().setEntityFlags((byte) 32);
            w.getHelper().setWitherInvulnerableTime(880); // Magic number
            return new PacketPlayOutSpawnEntityLiving(entityId, new UUID(0, 0), EntityType.WITHER, new Location(null, 0,0,0), w).build();
        } else {
            return new PacketPlayOutEntityMetadata(entityId, w).build();
        }
    }

    /**
     * A method taken from minecraft code used to convert double to int
     *
     * @param   paramDouble
     *          double value
     * @return  int value
     */
    private int floor(double paramDouble) {
        int i = (int)paramDouble;
        return paramDouble < i ? i - 1 : i;
    }

    /**
     * Converts TAB's IChatBaseComponent into minecraft's component using String deserialization.
     * If the requested component is found in cache, it is returned. If not, it is created, added into cache and returned.
     * If {@code component} is {@code null}, returns {@code null}
     *
     * @param   component
     *          component to convert
     * @param   clientVersion
     *          client version used to decide RGB conversion
     * @return  converted component or {@code null} if {@code component} is {@code null}
     */
    public Object toNMSComponent(IChatBaseComponent component, ProtocolVersion clientVersion) {
        if (component instanceof WrappedChatComponent) return ((WrappedChatComponent) component).getOriginalComponent();
        return componentCache.get(component, clientVersion);
    }

    /**
     * Creates a new Scoreboard Objective with given name.
     *
     * @param   objectiveName
     *          Objective name
     * @return  NMS Objective
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    private Object newScoreboardObjective(String objectiveName) throws ReflectiveOperationException {
        if (nms.getMinorVersion() >= 13) {
            return nms.newScoreboardObjective.newInstance(null, objectiveName, null, toNMSComponent(new IChatBaseComponent(""), TabAPI.getInstance().getServerVersion()), null);
        }
        return nms.newScoreboardObjective.newInstance(null, objectiveName, nms.IScoreboardCriteria_self.get(null));
    }
}