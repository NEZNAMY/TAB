package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitPipelineInjector;
import me.neznamy.tab.platforms.bukkit.BukkitTabList;
import me.neznamy.tab.platforms.bukkit.header.HeaderFooter;
import me.neznamy.tab.platforms.bukkit.scoreboard.ScoreboardLoader;
import me.neznamy.tab.shared.ProtocolVersion;

/**
 * Class holding all NMS classes, methods, fields and constructors used by TAB.
 */
public class NMSStorage {

    /** Instance of this class */
    @Getter @Setter private static NMSStorage instance;

    /**
     * Constructs new instance and attempts to load all fields, methods and constructors.
     */
    @SneakyThrows
    public NMSStorage() {
        int minorVersion = BukkitReflection.getMinorVersion();
        ProtocolVersion.UNKNOWN_SERVER_VERSION.setMinorVersion(minorVersion); //fixing compatibility with forks that set version field value to "Unknown"
        DataWatcher.load();
        PacketEntityView.load();
        PingRetriever.tryLoad();
        PacketSender.tryLoad();
        ScoreboardLoader.findInstance();
        if (minorVersion >= 8) {
            BukkitPipelineInjector.tryLoad();
            HeaderFooter.findInstance();
            BukkitTabList.load();
        }
    }
}