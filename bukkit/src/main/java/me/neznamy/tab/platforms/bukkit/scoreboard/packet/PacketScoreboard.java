package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.util.ComponentCache;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Scoreboard implementation which uses packets
 * to send scoreboards to use the full potential on all versions
 * and server software without any artificial limits.
 */
public class PacketScoreboard extends Scoreboard<BukkitTabPlayer> {

    @Getter
    private static boolean available;

    @Getter
    private static Exception exception;

    static Class<?> Component;
    static Class<?> Scoreboard;
    static Class<?> ScoreboardObjective;
    static Object emptyScoreboard;
    static Class<?> NumberFormat;
    private static Constructor<?> newFixedFormat;

    // Objective packet
    private static Class<?> ObjectivePacketClass;
    private static Constructor<?> newObjectivePacket;
    private static Field Objective_OBJECTIVE_NAME;
    private static Field Objective_METHOD;
    private static Field Objective_RENDER_TYPE;
    static Constructor<?> newScoreboardObjective;
    private static Method ScoreboardObjective_setDisplayName;
    private static Enum<?>[] healthDisplays;
    static Object IScoreboardCriteria_dummy;

    private static ScorePacketData scorePacketData;
    @Getter private static TeamPacketData teamPacketData;
    @Getter private static DisplayPacketData displayPacketData;
    private static PacketSender packetSender;

    private static Method ChatSerializer_DESERIALIZE;
    private static final ComponentCache<TabComponent, Object> componentCache = new ComponentCache<>(1000,
            (component, clientVersion) -> ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));

    static {
        try {
            int minorVersion = BukkitReflection.getMinorVersion();
            Scoreboard = BukkitReflection.getClass("world.scores.Scoreboard", "Scoreboard");
            ScoreboardObjective = BukkitReflection.getClass("world.scores.Objective", "world.scores.ScoreboardObjective", "ScoreboardObjective");
            Class<?> IScoreboardCriteria = BukkitReflection.getClass(
                    "world.scores.criteria.ObjectiveCriteria", // Mojang mapped
                    "world.scores.criteria.IScoreboardCriteria", // Bukkit 1.17.+
                    "IScoreboardCriteria", // 1.5.1 - 1.16.5
                    "IObjective" // 1.5.0
            );
            ObjectivePacketClass = BukkitReflection.getClass(
                    "network.protocol.game.ClientboundSetObjectivePacket", // Mojang mapped
                    "network.protocol.game.PacketPlayOutScoreboardObjective", // Bukkit 1.17+
                    "PacketPlayOutScoreboardObjective", // 1.7 - 1.16.5
                    "Packet206SetScoreboardObjective" // 1.5 - 1.6.4
            );
            emptyScoreboard = Scoreboard.getConstructor().newInstance();
            Objective_OBJECTIVE_NAME = ReflectionUtils.getFields(ObjectivePacketClass, String.class).get(0);
            List<Field> list = ReflectionUtils.getFields(ObjectivePacketClass, int.class);
            Objective_METHOD = list.get(list.size()-1);
            newObjectivePacket = ObjectivePacketClass.getConstructor(ScoreboardObjective, int.class);
            IScoreboardCriteria_dummy = ReflectionUtils.getFields(IScoreboardCriteria, IScoreboardCriteria).get(0).get(null);
            newScoreboardObjective = ReflectionUtils.getOnlyConstructor(ScoreboardObjective);
            if (minorVersion >= 7) {
                Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
                Class<?> ChatSerializer = BukkitReflection.getClass("network.chat.Component$Serializer",
                        "network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
                ChatSerializer_DESERIALIZE = ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class).get(0);
            }
            if (minorVersion >= 8) {
                Class<?> EnumScoreboardHealthDisplay = BukkitReflection.getClass(
                        "world.scores.criteria.ObjectiveCriteria$RenderType",
                        "world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay",
                        "IScoreboardCriteria$EnumScoreboardHealthDisplay",
                        "EnumScoreboardHealthDisplay");
                healthDisplays = (Enum<?>[]) EnumScoreboardHealthDisplay.getMethod("values").invoke(null);
                if (minorVersion < 13) {
                    Objective_RENDER_TYPE = ReflectionUtils.getOnlyField(ObjectivePacketClass, EnumScoreboardHealthDisplay);
                }
            }
            if (minorVersion < 13) {
                ScoreboardObjective_setDisplayName = ReflectionUtils.getOnlyMethod(ScoreboardObjective, void.class, String.class);
            }
            if (BukkitReflection.is1_20_3Plus()) {
                NumberFormat = BukkitReflection.getClass("network.chat.numbers.NumberFormat");
                newFixedFormat = BukkitReflection.getClass("network.chat.numbers.FixedFormat").getConstructor(Component);
            }
            scorePacketData = new ScorePacketData();
            teamPacketData = new TeamPacketData();
            displayPacketData = new DisplayPacketData();
            packetSender = new PacketSender();
            available = true;
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public PacketScoreboard(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot0(int slot, @NotNull String objective) {
        packetSender.sendPacket(player.getPlayer(), displayPacketData.setDisplaySlot(slot, newObjective(objective, "", 0, null)));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                   @Nullable TabComponent numberFormat) {
        packetSender.sendPacket(player.getPlayer(), newObjectivePacket(ObjectiveAction.REGISTER, objectiveName, title, display, numberFormat));
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        packetSender.sendPacket(player.getPlayer(), newObjectivePacket(ObjectiveAction.UNREGISTER, objectiveName, "", 0, null));
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                 @Nullable TabComponent numberFormat) {
        packetSender.sendPacket(player.getPlayer(), newObjectivePacket(ObjectiveAction.UPDATE, objectiveName, title, display, numberFormat));
    }

    @SneakyThrows
    private Object newObjectivePacket(int action, @NotNull String objectiveName, @NotNull String title, int display,
                                      @Nullable TabComponent numberFormat) {
        Object packet = newObjectivePacket.newInstance(newObjective(objectiveName, title, display, numberFormat), action);
        if (BukkitReflection.getMinorVersion() >= 8 && BukkitReflection.getMinorVersion() < 13) {
            Objective_RENDER_TYPE.set(packet, healthDisplays[display]);
        }
        return packet;
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color) {
        packetSender.sendPacket(player.getPlayer(), teamPacketData.registerTeam(name, prefix, toComponent(prefix), suffix,
                toComponent(suffix), visibility, collision, players, options, color));
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        packetSender.sendPacket(player.getPlayer(), teamPacketData.unregisterTeam(name));
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                            int options, @NotNull EnumChatFormat color) {
        packetSender.sendPacket(player.getPlayer(), teamPacketData.updateTeam(name, prefix, toComponent(prefix), suffix,
                toComponent(suffix), visibility, collision, options, color));
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String scoreHolder, int score,
                          @Nullable TabComponent displayName, @Nullable TabComponent numberFormat) {
        packetSender.sendPacket(player.getPlayer(), scorePacketData.setScore(objective, scoreHolder, score, toComponent(displayName), toFixedFormat(numberFormat)));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String scoreHolder) {
        packetSender.sendPacket(player.getPlayer(), scorePacketData.removeScore(objective, scoreHolder));
    }

    @Override
    public boolean isTeamPacket(@NotNull Object packet) {
        return teamPacketData.getTeamPacketClass().isInstance(packet);
    }

    @Override
    public void onTeamPacket(@NotNull Object team) {
        teamPacketData.onTeamPacket(team);
    }

    @Override
    public boolean isDisplayObjective(@NotNull Object packet) {
        return displayPacketData.isDisplayObjective(packet);
    }

    @Override
    public void onDisplayObjective(@NotNull Object packet) {
        displayPacketData.onDisplayObjective(player, packet);
    }

    @Override
    public boolean isObjective(@NotNull Object packet) {
        return ObjectivePacketClass.isInstance(packet);
    }

    @Override
    @SneakyThrows
    public void onObjective(@NotNull Object packet) {
        TAB.getInstance().getFeatureManager().onObjective(player,
                Objective_METHOD.getInt(packet), (String) Objective_OBJECTIVE_NAME.get(packet));
    }

    /**
     * Creates a new Scoreboard Objective with given parameters.
     *
     * @param   objectiveName
     *          Objective name
     * @param   title
     *          Objective title
     * @param   renderType
     *          Render type
     * @param   numberFormat
     *          Default number format (1.20.3+)
     * @return  Created objective
     */
    @SneakyThrows
    public Object newObjective(@NotNull String objectiveName, @NotNull String title, int renderType,
                               @Nullable TabComponent numberFormat) {
        if (BukkitReflection.is1_20_3Plus()) {
            // 1.20.3+
            return newScoreboardObjective.newInstance(
                    emptyScoreboard,
                    objectiveName,
                    null, // Criteria
                    toComponent(title),
                    healthDisplays[renderType],
                    false, // Auto update
                    toFixedFormat(numberFormat)
            );
        }
        if (BukkitReflection.getMinorVersion() >= 13) {
            // 1.13 - 1.20.2
            return newScoreboardObjective.newInstance(
                    emptyScoreboard,
                    objectiveName,
                    null, // Criteria
                    toComponent(title),
                    healthDisplays[renderType]
            );
        }
        // 1.5 - 1.12.2
        Object objective = newScoreboardObjective.newInstance(emptyScoreboard, objectiveName, IScoreboardCriteria_dummy);
        ScoreboardObjective_setDisplayName.invoke(objective, title);
        return objective;
    }

    @Nullable
    private Object toComponent(@NotNull String text) {
        return toComponent(TabComponent.optimized(text));
    }

    @Nullable
    private Object toComponent(@Nullable TabComponent component) {
        if (component == null || BukkitReflection.getMinorVersion() < 8) return null;
        return componentCache.get(component, player.getVersion());
    }

    @Nullable
    @SneakyThrows
    private Object toFixedFormat(@Nullable TabComponent numberFormat) {
        if (numberFormat == null || newFixedFormat == null) return null;
        return newFixedFormat.newInstance(toComponent(numberFormat));
    }
}
