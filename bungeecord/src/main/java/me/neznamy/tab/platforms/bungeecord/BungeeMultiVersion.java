package me.neznamy.tab.platforms.bungeecord;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard.*;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.*;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Collectors;

public class BungeeMultiVersion {

    /**
     * Flag tracking if the current BungeeCord version contains changes made in build 1637.
     * This build adds 1.19 support and moves class
     * "net.md_5.bungee.connection.LoginResult.Property" to "net.md_5.bungee.protocol.Property".
     */
    private static final boolean b1637Plus = ReflectionUtils.classExists("net.md_5.bungee.protocol.Property");

    /**
     * Flag tracking if the current BungeeCord version contains changes made in build 1671.
     * This build adds 1.19.3 support, adds new PlayerListItem classes and Item class changes.
     */
    private static final boolean b1671Plus = ReflectionUtils.classExists("net.md_5.bungee.protocol.packet.PlayerListItemUpdate");

    /**
     * Flag tracking if the current BungeeCord version contains changes made in build 1732.
     * This build changes "ScoreboardDisplay" field "position" from "byte" to "int".
     */
    private static final boolean b1732Plus = ReflectionUtils.getFields(ScoreboardDisplay.class, int.class).size() > 0;

    /**
     * Flag tracking if the current BungeeCord version contains changes made in build 1738.
     * This build adds "sendPacketQueued" method to UserConnection class.
     */
    private static final boolean b1738Plus = ReflectionUtils.methodExists(UserConnection.class, "sendPacketQueued", DefinedPacket.class);

    /**
     * Flag tracking if the current BungeeCord version contains changes made in build 1760.
     * This build changes all packet fields for displaying information from String to BaseComponent.
     */
    private static final boolean b1760Plus = ReflectionUtils.methodExists(BossBar.class, "setTitle", BaseComponent.class);

    /**
     * Flag tracking if the current BungeeCord version contains changes made in build 1767.
     * This build fixes packet serialization by adding Either[String, BaseComponent] class for display values.
     */
    private static final boolean b1767Plus = ReflectionUtils.classExists("net.md_5.bungee.protocol.Either");

    private static Class<?> Either;
    private static Method Either_left;
    private static Method Either_right;

    static {
        try {
            if (b1767Plus) {
                Either = Class.forName("net.md_5.bungee.protocol.Either");
                Either_left = Either.getMethod("left", Object.class);
                Either_right = Either.getMethod("right", Object.class);
            }
        } catch (Exception e) {
            ProxyServer.getInstance().getLogger().warning(EnumChatFormat.RED + "[TAB] Failed to initialize bungee internal fields");
        }
    }

    @SneakyThrows
    public static void setProperties(@NotNull Item item, String[]... properties) {
        if (b1637Plus) {
            Property[] array = new Property[properties.length];
            for (int i = 0; i < properties.length; i++) {
                array[i] = new Property(properties[i][0], properties[i][1], properties[i][2]);
            }
            item.setProperties(array);
        } else {
            String[][] array = new String[properties.length][3];
            for (int i = 0; i < properties.length; i++) {
                String[] property = properties[i];
                array[i][0] = property[0];
                array[i][1] = property[1];
                array[i][2] = property[2];
            }
            Item.class.getMethod("setProperties", String[][].class).invoke(item, (Object) array);
        }
    }

    @SneakyThrows
    public static void setListed(@NotNull Item item, Boolean listed) {
        if (b1671Plus) {
            item.setListed(listed);
        }
    }

    @SneakyThrows
    public static void setGamemode(@NotNull Item item, int gameMode) {
        if (b1671Plus) {
            item.setGamemode(gameMode);
        } else {
            Item.class.getMethod("setGamemode", int.class).invoke(item, gameMode);
        }
    }

    @SneakyThrows
    public static void setPing(@NotNull Item item, int ping) {
        if (b1671Plus) {
            item.setPing(ping);
        } else {
            Item.class.getMethod("setPing", int.class).invoke(item, ping);
        }
    }

    @SneakyThrows
    @NotNull
    public static ScoreboardDisplay newScoreboardDisplay(int position, @NotNull String name) {
        if (b1732Plus) {
            return ScoreboardDisplay.class.getConstructor(int.class, String.class).newInstance(position, name);
        } else {
            return ScoreboardDisplay.class.getConstructor(byte.class, String.class).newInstance((byte) position, name);
        }
    }

    @SneakyThrows
    public static void sendPacket(@NotNull BungeeTabPlayer player, @NotNull DefinedPacket packet) {
        if (b1738Plus) {
            try {
                UserConnection.class.getDeclaredMethod("sendPacketQueued", DefinedPacket.class).invoke(player.getPlayer(), packet);
            } catch (InvocationTargetException BungeeCordBug) { // Since we use reflection we must catch this one
                // java.lang.NullPointerException: Cannot invoke "net.md_5.bungee.protocol.MinecraftEncoder.getProtocol()" because the return value of "io.netty.channel.ChannelPipeline.get(java.lang.Class)" is null
                //        at net.md_5.bungee.netty.ChannelWrapper.getEncodeProtocol(ChannelWrapper.java:51)
                //        at net.md_5.bungee.UserConnection.sendPacketQueued(UserConnection.java:194)
                if (TAB.getInstance().getConfiguration().isDebugMode()) {
                    TAB.getInstance().getErrorManager().printError("Failed to deliver packet to player " + player.getName() +
                            " (online = " + player.getPlayer().isConnected() + "): " + packet, BungeeCordBug);
                }
            }
        } else {
            player.getPlayer().unsafe().sendPacket(packet);
        }
    }

    @SneakyThrows
    public static void setDisplayName(@NotNull Item item, @NotNull IChatBaseComponent displayName, @NotNull ProtocolVersion version) {
        if (b1760Plus) {
            Item.class.getMethod("setDisplayName", BaseComponent.class).invoke(item, toComponent(displayName, version));
        } else if (version.getMinorVersion() >= 8) {
            Item.class.getMethod("setDisplayName", String.class).invoke(item, displayName.toString(version));
        } else {
            Item.class.getMethod("setDisplayName", String.class).invoke(item, displayName.toLegacyText());
        }
    }

    @SneakyThrows
    @NotNull
    public static PlayerListHeaderFooter newPlayerListHeaderFooter(@NotNull IChatBaseComponent header,
                                                                   @NotNull IChatBaseComponent footer,
                                                                   @NotNull ProtocolVersion version) {
        if (b1760Plus) {
            return PlayerListHeaderFooter.class.getConstructor(
                    BaseComponent.class,
                    BaseComponent.class
            ).newInstance(
                    toComponent(header, version),
                    toComponent(footer, version)
            );
        } else {
            return PlayerListHeaderFooter.class.getConstructor(
                    String.class,
                    String.class
            ).newInstance(
                    header.toString(version),
                    footer.toString(version)
            );
        }
    }

    @SneakyThrows
    public static void setTitle(@NotNull BossBar bossBar, @NotNull String title, @NotNull ProtocolVersion version) {
        if (b1760Plus) {
            BossBar.class.getMethod("setTitle", BaseComponent.class)
                    .invoke(bossBar, toComponent(IChatBaseComponent.optimizedComponent(title), version));
        } else {
            BossBar.class.getMethod("setTitle", String.class)
                    .invoke(bossBar, IChatBaseComponent.optimizedComponent(title).toString(version));
        }
    }

    @NotNull
    @SneakyThrows
    public static ScoreboardObjective newScoreboardObjective(@NotNull String objective, @NotNull String title,
                                                             @Nullable ScoreboardObjective.HealthDisplay healthDisplay, ObjectiveAction action,
                                                             @NotNull ProtocolVersion version) {
        if (b1767Plus) {
            return ScoreboardObjective.class.getConstructor(
                    String.class,
                    Either,
                    ScoreboardObjective.HealthDisplay.class,
                    byte.class
            ).newInstance(
                    objective,
                    either(title, version),
                    healthDisplay,
                    (byte) action.ordinal()
            );
        } else if (b1760Plus) {
            return ScoreboardObjective.class.getConstructor(
                    String.class,
                    BaseComponent.class,
                    ScoreboardObjective.HealthDisplay.class,
                    byte.class
            ).newInstance(
                    objective,
                    toComponent(IChatBaseComponent.optimizedComponent(title), version),
                    healthDisplay,
                    (byte) action.ordinal()
            );
        } else {
            return ScoreboardObjective.class.getConstructor(
                    String.class,
                    String.class,
                    ScoreboardObjective.HealthDisplay.class,
                    byte.class
            ).newInstance(
                    objective,
                    jsonOrRaw(title, version),
                    healthDisplay,
                    (byte) action.ordinal()
            );
        }
    }

    @NotNull
    @SneakyThrows
    public static Team newTeam(@NotNull String name, TeamAction action, @NotNull String prefix, @NotNull String suffix,
                               @NotNull String nameTagVisibility, @NotNull String collisionRule, int color,
                               byte options, @Nullable String[] players, @NotNull ProtocolVersion version) {
        if (b1767Plus) {
            return Team.class.getConstructor(
                    String.class,
                    byte.class,
                    Either,
                    Either,
                    Either,
                    String.class,
                    String.class,
                    int.class,
                    byte.class,
                    String[].class
            ).newInstance(
                    name,
                    (byte) action.ordinal(),
                    either(name, version),
                    either(prefix, version),
                    either(suffix, version),
                    nameTagVisibility,
                    collisionRule,
                    color,
                    options,
                    players
            );
        } else if (b1760Plus) {
            return Team.class.getConstructor(
                    String.class,
                    byte.class,
                    BaseComponent.class,
                    BaseComponent.class,
                    BaseComponent.class,
                    String.class,
                    String.class,
                    int.class,
                    byte.class,
                    String[].class
            ).newInstance(
                    name,
                    (byte) action.ordinal(),
                    toComponent(IChatBaseComponent.optimizedComponent(name), version),
                    toComponent(IChatBaseComponent.optimizedComponent(prefix), version),
                    toComponent(IChatBaseComponent.optimizedComponent(suffix), version),
                    nameTagVisibility,
                    collisionRule,
                    color,
                    options,
                    players
            );
        } else {
            return Team.class.getConstructor(
                    String.class,
                    byte.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    int.class,
                    byte.class,
                    String[].class
            ).newInstance(
                    name,
                    (byte) action.ordinal(),
                    jsonOrRaw(name, version),
                    jsonOrRaw(prefix, version),
                    jsonOrRaw(suffix, version),
                    nameTagVisibility,
                    collisionRule,
                    color,
                    options,
                    players
            );
        }
    }

    /**
     * If player's version is 1.13+, creates a component from given text and returns
     * it as a serialized component, which BungeeCord uses.
     * <p>
     * If player's version is 1.12-, the text is returned
     *
     * @param   text
     *          Text to convert
     * @param   version
     *          Protocol version to create json string for
     * @return  serialized component for 1.13+ clients, cut string for 1.12-
     */
    @NotNull
    private static String jsonOrRaw(@NotNull String text, @NotNull ProtocolVersion version) {
        if (version.getMinorVersion() >= 13) {
            return IChatBaseComponent.optimizedComponent(text).toString(version);
        } else {
            return text;
        }
    }

    @SneakyThrows
    private static Object either(@NotNull String text, @NotNull ProtocolVersion version) {
        if (version.getMinorVersion() >= 13) {
            return Either_right.invoke(null, toComponent(IChatBaseComponent.optimizedComponent(text), version));
        } else {
            return Either_left.invoke(null, text);
        }
    }

    /**
     * Converts internal component class to platform's component class
     *
     * @param   component
     *          Component to convert
     * @param   version
     *          Game version to convert component for
     * @return  Converted component
     */
    private static BaseComponent toComponent(@NotNull IChatBaseComponent component, @NotNull ProtocolVersion version) {
        TextComponent textComponent = new TextComponent(component.getText());
        ChatModifier modifier = component.getModifier();
        if (modifier.getColor() != null) textComponent.setColor(ChatColor.of(
                modifier.getColor().toString(version.getMinorVersion() >= 16)));
        if (modifier.isBold()) textComponent.setBold(true);
        if (modifier.isItalic()) textComponent.setItalic(true);
        if (modifier.isObfuscated()) textComponent.setObfuscated(true);
        if (modifier.isStrikethrough()) textComponent.setStrikethrough(true);
        if (modifier.isUnderlined()) textComponent.setUnderlined(true);
        if (!component.getExtra().isEmpty()) textComponent.setExtra(
                component.getExtra().stream().map(c -> toComponent(c, version)).collect(Collectors.toList()));
        return textComponent;
    }
}
