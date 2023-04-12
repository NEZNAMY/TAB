package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketPlayOutScoreboardTeamStorage {

    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Method Constructor_of;
    public static Method Constructor_ofBoolean;
    public static Method Constructor_ofString;
    public static Field NAME;
    public static Field ACTION;
    public static Field PLAYERS;
    public static Class<Enum> PlayerAction;

    public static Class<Enum> EnumNameTagVisibility;
    public static Class<Enum> EnumTeamPush;

    public static Class<?> ScoreboardTeam;
    public static Constructor<?> newScoreboardTeam;
    public static Method ScoreboardTeam_getPlayerNameSet;
    public static Method ScoreboardTeam_setNameTagVisibility;
    public static Method ScoreboardTeam_setCollisionRule;
    public static Method ScoreboardTeam_setPrefix;
    public static Method ScoreboardTeam_setSuffix;
    public static Method ScoreboardTeam_setColor;
    public static Method ScoreboardTeam_setAllowFriendlyFire;
    public static Method ScoreboardTeam_setCanSeeFriendlyInvisibles;

    public static void load(NMSStorage nms) throws NoSuchMethodException {
        newScoreboardTeam = ScoreboardTeam.getConstructor(nms.Scoreboard, String.class);
        NAME = ReflectionUtils.getFields(CLASS, String.class).get(0);
        ACTION = ReflectionUtils.getInstanceIntFields(CLASS, int.class).get(0);
        PLAYERS = ReflectionUtils.getFields(CLASS, Collection.class).get(0);
        ScoreboardTeam_getPlayerNameSet = ReflectionUtils.getMethods(ScoreboardTeam, Collection.class).get(0);
        if (nms.getMinorVersion() >= 9) {
            ScoreboardTeam_setCollisionRule = ReflectionUtils.getMethods(ScoreboardTeam, void.class, EnumTeamPush).get(0);
        }
        if (nms.getMinorVersion() >= 13) {
            ScoreboardTeam_setColor = ReflectionUtils.getMethods(ScoreboardTeam, void.class, nms.EnumChatFormat).get(0);
        }
        if (nms.getMinorVersion() >= 17) {
            Constructor_of = ReflectionUtils.getMethods(CLASS, CLASS, ScoreboardTeam).get(0);
            Constructor_ofBoolean = ReflectionUtils.getMethods(CLASS, CLASS, ScoreboardTeam, boolean.class).get(0);
            Constructor_ofString = ReflectionUtils.getMethods(CLASS, CLASS, ScoreboardTeam, String.class, PlayerAction).get(0);
        } else {
            CONSTRUCTOR = CLASS.getConstructor(ScoreboardTeam, int.class);
        }
    }

    private static void createTeamModern(Object team, ProtocolVersion clientVersion, String prefix, String suffix, String visibility, String collision) throws ReflectiveOperationException {
        NMSStorage nms = NMSStorage.getInstance();
        if (prefix != null) ScoreboardTeam_setPrefix.invoke(team, nms.toNMSComponent(IChatBaseComponent.optimizedComponent(prefix), clientVersion));
        if (suffix != null) ScoreboardTeam_setSuffix.invoke(team, nms.toNMSComponent(IChatBaseComponent.optimizedComponent(suffix), clientVersion));
        ScoreboardTeam_setColor.invoke(team, Enum.valueOf(nms.EnumChatFormat, EnumChatFormat.lastColorsOf(prefix).toString()));
        ScoreboardTeam_setNameTagVisibility.invoke(team, Enum.valueOf(EnumNameTagVisibility, String.valueOf(visibility).equals("always") ? "ALWAYS" : "NEVER"));
        ScoreboardTeam_setCollisionRule.invoke(team, Enum.valueOf(EnumTeamPush, String.valueOf(collision).equals("always") ? "ALWAYS" : "NEVER"));
    }

    private static void createTeamLegacy(Object team, String prefix, String suffix, String visibility, String collision) throws ReflectiveOperationException {
        NMSStorage nms = NMSStorage.getInstance();
        if (prefix != null) ScoreboardTeam_setPrefix.invoke(team, prefix);
        if (suffix != null) ScoreboardTeam_setSuffix.invoke(team, suffix);
        if (nms.getMinorVersion() >= 8) ScoreboardTeam_setNameTagVisibility.invoke(team, Enum.valueOf(EnumNameTagVisibility, String.valueOf(visibility).equals("always") ? "ALWAYS" : "NEVER"));
        if (nms.getMinorVersion() >= 9) ScoreboardTeam_setCollisionRule.invoke(team, Enum.valueOf(EnumTeamPush, String.valueOf(collision).equals("always") ? "ALWAYS" : "NEVER"));
    }

    public static Object register(String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options, ProtocolVersion clientVersion) {
        try {
            NMSStorage nms = NMSStorage.getInstance();
            Object team = newScoreboardTeam.newInstance(nms.emptyScoreboard, name);
            ((Collection<String>)ScoreboardTeam_getPlayerNameSet.invoke(team)).addAll(players);
            ScoreboardTeam_setAllowFriendlyFire.invoke(team, (options & 0x1) > 0);
            ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(team, (options & 0x2) > 0);
            if (nms.getMinorVersion() >= 13) {
                createTeamModern(team, clientVersion, prefix, suffix, visibility, collision);
            } else {
                createTeamLegacy(team, prefix, suffix, visibility, collision);
            }
            if (nms.getMinorVersion() >= 17) {
                return Constructor_ofBoolean.invoke(null, team, true);
            }
            return CONSTRUCTOR.newInstance(team, 0);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Object unregister(String name) {
        try {
            NMSStorage nms = NMSStorage.getInstance();
            Object team = newScoreboardTeam.newInstance(nms.emptyScoreboard, name);
            if (nms.getMinorVersion() >= 17) {
                return Constructor_of.invoke(null, team);
            } else {
                return CONSTRUCTOR.newInstance(team, 1);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Object update(String name, String prefix, String suffix, String visibility, String collision, int options, ProtocolVersion clientVersion) {
        try {
            NMSStorage nms = NMSStorage.getInstance();
            Object team = newScoreboardTeam.newInstance(nms.emptyScoreboard, name);
            ScoreboardTeam_setAllowFriendlyFire.invoke(team, (options & 0x1) > 0);
            ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(team, (options & 0x2) > 0);
            if (nms.getMinorVersion() >= 13) {
                createTeamModern(team, clientVersion, prefix, suffix, visibility, collision);
            } else {
                createTeamLegacy(team, prefix, suffix, visibility, collision);
            }
            if (nms.getMinorVersion() >= 17) {
                return Constructor_ofBoolean.invoke(null, team, false);
            }
            return CONSTRUCTOR.newInstance(team, 2);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
