package me.neznamy.tab.platforms.bukkit.provider;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.scoreboard.viaversion.ViaScoreboard113;
import me.neznamy.tab.platforms.bukkit.scoreboard.viaversion.ViaScoreboard116;
import me.neznamy.tab.platforms.bukkit.scoreboard.viaversion.ViaScoreboard1203;
import me.neznamy.tab.platforms.bukkit.tablist.viaversion.ViaTabList116;
import me.neznamy.tab.platforms.bukkit.tablist.viaversion.ViaTabList1193;
import me.neznamy.tab.platforms.bukkit.tablist.viaversion.ViaTabList1212;
import me.neznamy.tab.platforms.bukkit.tablist.viaversion.ViaTabList1214;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation for sending new features on old servers using ViaVersion API.
 * If no implementation is required for a player, {@code null} is returned.
 */
@RequiredArgsConstructor
public class ViaVersionImplementationProvider {

    /** Map of tablist implementations by each new version */
    private static final Map<ProtocolVersion, Function<BukkitTabPlayer, TabList>> TABLIST_IMPLEMENTATIONS = new LinkedHashMap<>();

    /** Map of scoreboard implementations by each new version */
    private static final Map<ProtocolVersion, Function<BukkitTabPlayer, Scoreboard>> SCOREBOARD_IMPLEMENTATIONS = new LinkedHashMap<>();

    static {
        TABLIST_IMPLEMENTATIONS.put(ProtocolVersion.V1_21_4, ViaTabList1214::new);
        TABLIST_IMPLEMENTATIONS.put(ProtocolVersion.V1_21_2, ViaTabList1212::new);
        TABLIST_IMPLEMENTATIONS.put(ProtocolVersion.V1_19_3, ViaTabList1193::new);
        TABLIST_IMPLEMENTATIONS.put(ProtocolVersion.V1_16, ViaTabList116::new);

        SCOREBOARD_IMPLEMENTATIONS.put(ProtocolVersion.V1_20_3, ViaScoreboard1203::new);
        SCOREBOARD_IMPLEMENTATIONS.put(ProtocolVersion.V1_16, ViaScoreboard116::new);
        SCOREBOARD_IMPLEMENTATIONS.put(ProtocolVersion.V1_13, ViaScoreboard113::new);
    }

    private final ProtocolVersion serverVersion;

    @Nullable
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
        if (!ReflectionUtils.classExists("com.viaversion.viaversion.protocols.v1_20_2to1_20_3.Protocol1_20_2To1_20_3")) {
            return null;
        }
        int serverVer = serverVersion.getNetworkId();
        int playerVer = player.getVersion().getNetworkId();
        for (Map.Entry<ProtocolVersion, Function<BukkitTabPlayer, Scoreboard>> entry : SCOREBOARD_IMPLEMENTATIONS.entrySet()) {
            if (serverVer < entry.getKey().getNetworkId() && playerVer >= entry.getKey().getNetworkId()) {
                return entry.getValue().apply(player);
            }
        }
        return null;
    }

    @Nullable
    public TabList newTabList(@NotNull BukkitTabPlayer player) {
        if (!ReflectionUtils.classExists("com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4")) {
            return null;
        }
        int serverVer = serverVersion.getNetworkId();
        int playerVer = player.getVersion().getNetworkId();
        for (Map.Entry<ProtocolVersion, Function<BukkitTabPlayer, TabList>> entry : TABLIST_IMPLEMENTATIONS.entrySet()) {
            if (serverVer < entry.getKey().getNetworkId() && playerVer >= entry.getKey().getNetworkId()) {
                return entry.getValue().apply(player);
            }
        }
        return null;
    }
}
