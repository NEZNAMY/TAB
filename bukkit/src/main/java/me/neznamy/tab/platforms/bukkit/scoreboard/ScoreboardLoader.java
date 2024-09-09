package me.neznamy.tab.platforms.bukkit.scoreboard;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.impl.DummyScoreboard;
import me.neznamy.tab.shared.util.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.List;

/**
 * Loader finding the best available Scoreboard implementation.
 */
public class ScoreboardLoader {

    /** Instance function */
    @Getter
    @Setter
    @NotNull
    private static FunctionWithException<BukkitTabPlayer, Scoreboard> instance = DummyScoreboard::new;

    /**
     * Finds the best available instance for current server software.
     *
     * @param   serverVersion
     *          Server version
     */
    @SneakyThrows
    public static void findInstance(@NotNull ProtocolVersion serverVersion) {
        boolean versionCheck = EnumSet.of(
                ProtocolVersion.V1_20_5,
                ProtocolVersion.V1_20_6,
                ProtocolVersion.V1_21,
                ProtocolVersion.V1_21_1
        ).contains(serverVersion);
        if (ReflectionUtils.classExists("org.bukkit.craftbukkit.CraftServer") && versionCheck) {
            Constructor<?> constructor = Class.forName("me.neznamy.tab.platforms.paper.PaperPacketScoreboard").getConstructor(BukkitTabPlayer.class);
            instance = player -> (Scoreboard) constructor.newInstance(player);
        } else if (PacketScoreboard.isAvailable()) {
            instance = PacketScoreboard::new;
        } else if (PaperScoreboard.isAvailable()) {
            instance = PaperScoreboard::new;
            BukkitUtils.compatibilityError(PacketScoreboard.getException(), "Scoreboards", "Paper API", "Compatibility with other plugins being reduced");
        } else if (BukkitScoreboard.isAvailable()) {
            instance = BukkitScoreboard::new;
            List<String> missingFeatures = Lists.newArrayList(
                    "Compatibility with other plugins being reduced",
                    "Features receiving new artificial character limits"
            );
            if (BukkitReflection.is1_20_3Plus()) {
                missingFeatures.add("1.20.3+ visuals not working due to lack of API"); // soontm?
            }
            BukkitUtils.compatibilityError(PacketScoreboard.getException(), "Scoreboards", "Bukkit API", missingFeatures.toArray(new String[0]));
        } else {
            BukkitUtils.compatibilityError(PacketScoreboard.getException(), "Scoreboards", null,
                    "Scoreboard feature will not work",
                    "Belowname feature will not work",
                    "Player objective feature will not work",
                    "Scoreboard teams feature will not work (nametags & sorting)");
        }
    }
}
