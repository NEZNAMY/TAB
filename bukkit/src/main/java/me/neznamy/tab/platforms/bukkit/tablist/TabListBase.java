package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.header.HeaderFooter;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Base TabList class for all implementations.
 */
@RequiredArgsConstructor
public abstract class TabListBase implements TabList {

    /** Instance function */
    @Getter
    private static Function<BukkitTabPlayer, TabListBase> instance;

    @Nullable
    protected static SkinData skinData;

    /** Player this TabList belongs to */
    @NotNull
    protected final BukkitTabPlayer player;

    /**
     * Finds the best available instance for current server software.
     */
    public static void findInstance() {
        try {
            if (BukkitReflection.is1_19_3Plus()) {
                PacketTabList1193.load();
                instance = PacketTabList1193::new;
            } else if (BukkitReflection.getMinorVersion() >= 8) {
                PacketTabList18.load();
                instance = PacketTabList18::new;
            } else {
                PacketTabList17.load();
                instance = PacketTabList17::new;
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Failed to initialize NMS fields for " +
                    "tablist management due to a compatibility error. This will " +
                    "result in tablist-related features not working properly (Layout, spectator fix & ping spoof not working, " +
                    "tablist formatting being limited). " +
                    "Please update the plugin to version with proper support for your server version for optimal experience.");
            instance = BukkitTabList::new;
        }
    }

    @Override
    @SneakyThrows
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        if (HeaderFooter.getInstance() != null) HeaderFooter.getInstance().set(player, header, footer);
    }

    /**
     * Returns player's skin. If NMS fields did not load or server is in
     * offline mode, returns {@code null}.
     *
     * @return  Player's skin or {@code null}.
     */
    @Nullable
    public Skin getSkin() {
        if (skinData == null) return null;
        return skinData.getSkin(player.getPlayer());
    }
}
