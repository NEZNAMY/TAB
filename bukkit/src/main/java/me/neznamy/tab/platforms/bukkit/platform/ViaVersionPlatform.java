package me.neznamy.tab.platforms.bukkit.platform;

import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.scoreboard.viaversion.ViaScoreboard1203;
import me.neznamy.tab.platforms.bukkit.scoreboard.viaversion.ViaScoreboard113;
import me.neznamy.tab.platforms.bukkit.scoreboard.viaversion.ViaScoreboard116;
import me.neznamy.tab.platforms.bukkit.tablist.TabListBase;
import me.neznamy.tab.platforms.bukkit.tablist.viaversion.ViaTabList1193;
import me.neznamy.tab.platforms.bukkit.tablist.viaversion.ViaTabList1212;
import me.neznamy.tab.platforms.bukkit.tablist.viaversion.ViaTabList1214;
import me.neznamy.tab.platforms.bukkit.tablist.viaversion.ViaTabList116;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.jetbrains.annotations.NotNull;

public class ViaVersionPlatform {

    @NotNull
    public static FunctionWithException<BukkitTabPlayer, Scoreboard> findScoreboardProvider(@NotNull ProtocolVersion serverVersion) {
        if (!ReflectionUtils.classExists("com.viaversion.viaversion.protocols.v1_20_2to1_20_3.Protocol1_20_2To1_20_3")) {
            return FunctionWithException.empty();
        }
        if (serverVersion.getNetworkId() < ProtocolVersion.V1_13.getNetworkId()) {
            return player -> {
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
                    return new ViaScoreboard1203(player);
                }
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_16.getNetworkId()) {
                    return new ViaScoreboard116(player);
                }
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_13.getNetworkId()) {
                    return new ViaScoreboard113(player);
                }
                return null;
            };
        }
        if (serverVersion.getNetworkId() < ProtocolVersion.V1_16.getNetworkId()) {
            return player -> {
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
                    return new ViaScoreboard1203(player);
                }
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_16.getNetworkId()) {
                    return new ViaScoreboard116(player);
                }
                return null;
            };
        }
        if (serverVersion.getNetworkId() < ProtocolVersion.V1_20_3.getNetworkId()) {
            return player -> {
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
                    return new ViaScoreboard1203(player);
                }
                return null;
            };
        }
        return FunctionWithException.empty();
    }

    @NotNull
    public static FunctionWithException<BukkitTabPlayer, TabListBase> findTablistProvider(@NotNull ProtocolVersion serverVersion) {
        if (!ReflectionUtils.classExists("com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4")) {
            return FunctionWithException.empty();
        }
        if (serverVersion.getNetworkId() < ProtocolVersion.V1_16.getNetworkId()) {
            return player -> {
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_21_4.getNetworkId()) {
                    return new ViaTabList1214(player);
                }
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId()) {
                    return new ViaTabList1212(player);
                }
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
                    return new ViaTabList1193(player);
                }
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_16.getNetworkId()) {
                    return new ViaTabList116(player);
                }
                return null;
            };
        }
        if (serverVersion.getNetworkId() < ProtocolVersion.V1_19_3.getNetworkId()) {
            return player -> {
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_21_4.getNetworkId()) {
                    return new ViaTabList1214(player);
                }
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId()) {
                    return new ViaTabList1212(player);
                }
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
                    return new ViaTabList1193(player);
                }
                return null;
            };
        }
        if (serverVersion.getNetworkId() < ProtocolVersion.V1_21_2.getNetworkId()) {
            return player -> {
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_21_4.getNetworkId()) {
                    return new ViaTabList1214(player);
                }
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId()) {
                    return new ViaTabList1212(player);
                }
                return null;
            };
        }
        if (serverVersion.getNetworkId() < ProtocolVersion.V1_21_4.getNetworkId()) {
            return player -> {
                if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_21_4.getNetworkId()) {
                    return new ViaTabList1214(player);
                }
                return null;
            };
        }
        return FunctionWithException.empty();
    }
}
