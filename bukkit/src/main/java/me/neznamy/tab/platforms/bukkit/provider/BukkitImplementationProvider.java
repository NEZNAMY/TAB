package me.neznamy.tab.platforms.bukkit.provider;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.nms.converter.LegacyComponentConverter;
import me.neznamy.tab.platforms.bukkit.nms.converter.ModerateComponentConverter;
import me.neznamy.tab.platforms.bukkit.nms.converter.ModernComponentConverter;
import me.neznamy.tab.platforms.bukkit.scoreboard.BukkitScoreboard;
import me.neznamy.tab.platforms.bukkit.scoreboard.PaperScoreboard;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.platforms.bukkit.tablist.BukkitTabList;
import me.neznamy.tab.platforms.bukkit.tablist.PacketTabList1193;
import me.neznamy.tab.platforms.bukkit.tablist.PacketTabList17;
import me.neznamy.tab.platforms.bukkit.tablist.PacketTabList18;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.impl.DummyScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation provider using reflection for NMS / Bukkit implementations as fallback options.
 */
@Getter
public class BukkitImplementationProvider implements ImplementationProvider {

    @Nullable
    private final FunctionWithException<BukkitTabPlayer, Channel> channelFunction = findChannelFunction();

    @Nullable
    private final ComponentConverter componentConverter = findComponentConverter();

    @NotNull
    private final Function<BukkitTabPlayer, Scoreboard> scoreboardProvider = findScoreboardProvider();
    
    @NotNull
    private final Function<BukkitTabPlayer, TabList> tablistProvider = findTablistProvider();

    @Nullable
    private FunctionWithException<BukkitTabPlayer, Channel> findChannelFunction() {
        if (BukkitReflection.getMinorVersion() < 8) return null;
        try {
            Class<?> NetworkManager = BukkitReflection.getClass("network.Connection", "network.NetworkManager", "NetworkManager");
            Class<?> PlayerConnection = BukkitReflection.getClass("server.network.ServerGamePacketListenerImpl",
                    "server.network.PlayerConnection", "PlayerConnection");
            Class<?> EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
            Field PLAYER_CONNECTION = ReflectionUtils.getOnlyField(EntityPlayer, PlayerConnection);
            Field NETWORK_MANAGER;
            if (BukkitReflection.is1_20_2Plus()) {
                NETWORK_MANAGER = ReflectionUtils.getOnlyField(PlayerConnection.getSuperclass(), NetworkManager);
            } else {
                NETWORK_MANAGER = ReflectionUtils.getOnlyField(PlayerConnection, NetworkManager);
            }
            Field CHANNEL = ReflectionUtils.getOnlyField(NetworkManager, Channel.class);
            return player -> (Channel) CHANNEL.get(NETWORK_MANAGER.get(PLAYER_CONNECTION.get(player.getHandle())));
        } catch (Exception e) {
            BukkitUtils.compatibilityError(e, "network channel injection", null,
                    "Anti-override for tablist & nametags not working",
                    "Compatibility with nickname plugins changing player names will not work",
                    "Scoreboard will not be checking for other plugins");
            return null;
        }
    }

    @Nullable
    @SneakyThrows
    private ComponentConverter findComponentConverter() {
        try {
            if (BukkitReflection.getMinorVersion() >= 19) {
                // 1.19+
                return new ModernComponentConverter();
            } else if (BukkitReflection.getMinorVersion() >= 16) {
                // 1.16 - 1.18.2
                return new ModerateComponentConverter();
            } else {
                // 1.7 - 1.15.2
                return new LegacyComponentConverter();
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§c[TAB] Failed to initialize converter from TAB components to Minecraft components. " +
                    "This will negatively impact most features, see below.");
            if (BukkitUtils.PRINT_EXCEPTIONS) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @NotNull
    private Function<BukkitTabPlayer, Scoreboard> findScoreboardProvider() {
        try {
            if (BukkitReflection.getMinorVersion() >= 7) Objects.requireNonNull(componentConverter);
            PacketScoreboard.load();
            return PacketScoreboard::new;
        } catch (Exception e) {
            if (PaperScoreboard.isAvailable()) {
                BukkitUtils.compatibilityError(e, "Scoreboards", "Paper API", "Compatibility with other plugins being reduced");
                return PaperScoreboard::new;
            } else if (BukkitScoreboard.isAvailable()) {
                List<String> missingFeatures = Lists.newArrayList(
                        "Compatibility with other plugins being reduced",
                        "Features receiving new artificial character limits"
                );
                if (BukkitReflection.is1_20_3Plus()) {
                    missingFeatures.add("1.20.3+ visuals not working due to lack of API"); // soontm?
                }
                BukkitUtils.compatibilityError(e, "Scoreboards", "Bukkit API", missingFeatures.toArray(new String[0]));
                return BukkitScoreboard::new;
            } else if (BukkitReflection.getMinorVersion() >= 5) {
                BukkitUtils.compatibilityError(e, "Scoreboards", null,
                        "Scoreboard feature will not work",
                        "Belowname feature will not work",
                        "Player objective feature will not work",
                        "Scoreboard teams feature will not work (nametags & sorting)");
            }
        }
        return DummyScoreboard::new;
    }

    @NotNull
    @SneakyThrows
    private Function<BukkitTabPlayer, TabList> findTablistProvider() {
        try {
            if (ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket")) {
                // 1.19.3+
                Objects.requireNonNull(componentConverter);
                PacketTabList1193.loadNew();
                return PacketTabList1193::new;
            } else if (BukkitReflection.getMinorVersion() >= 8) {
                // 1.8 - 1.19.2
                Objects.requireNonNull(componentConverter);
                PacketTabList18.load();
                return PacketTabList18::new;
            } else {
                // 1.7.10 and lower
                PacketTabList17.load();
                return PacketTabList17::new;
            }
        } catch (Exception e) {
            BukkitUtils.compatibilityError(e, "tablist entry management", "Bukkit API",
                    "Layout feature will not work",
                    "Prevent-spectator-effect feature will not work",
                    "Ping spoof feature will not work",
                    "Tablist formatting missing anti-override",
                    "Tablist formatting not supporting relational placeholders");
            return BukkitTabList::new;
        }
    }

    @Override
    @NotNull
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
        return scoreboardProvider.apply(player);
    }

    @Override
    @NotNull
    public TabList newTabList(@NotNull BukkitTabPlayer player) {
        return tablistProvider.apply(player);
    }
}
