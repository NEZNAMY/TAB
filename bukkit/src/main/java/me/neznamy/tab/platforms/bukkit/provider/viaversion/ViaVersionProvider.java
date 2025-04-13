package me.neznamy.tab.platforms.bukkit.provider.viaversion;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
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
public class ViaVersionProvider {

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

    @NotNull
    private final ProtocolVersion serverVersion;

    @Nullable
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
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
