package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

/**
 * Base TabList class for all implementations.
 *
 * @param   <C>
 *          Component class
 */
public abstract class TabListBase<C> extends TabList<BukkitTabPlayer, C> {

    /** Instance function */
    @Getter
    private static Function<BukkitTabPlayer, TabListBase<?>> instance;

    @Nullable
    protected static SkinData skinData;

    @Nullable
    private static PacketHeaderFooter headerFooter;

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    protected TabListBase(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    /**
     * Finds the best available instance for current server software.
     */
    public static void findInstance() {
        try {
            if (BukkitReflection.is1_19_3Plus()) {
                PacketTabList1193.loadNew();
                instance = PacketTabList1193::new;
            } else if (BukkitReflection.getMinorVersion() >= 8) {
                PacketTabList18.load();
                instance = PacketTabList18::new;
            } else {
                PacketTabList17.load();
                instance = PacketTabList17::new;
            }
        } catch (Exception e) {
            BukkitUtils.compatibilityError(e, "tablist entry management", "Bukkit API",
                    "Layout feature will not work",
                    "Prevent-spectator-effect feature will not work",
                    "Ping spoof feature will not work",
                    "Tablist formatting missing anti-override",
                    "Tablist formatting not supporting relational placeholders");
            instance = BukkitTabList::new;
        }

        try {
            if (BukkitReflection.getMinorVersion() >= 8) {
                headerFooter = new PacketHeaderFooter();
            }
        } catch (Exception e) {
            BukkitUtils.compatibilityError(e, "sending header/footer", null,
                    "Header/footer will not work");
        }
    }

    @Override
    public void setPlayerListHeaderFooter0(@NonNull Object header, @NonNull Object footer) {
        if (headerFooter != null) headerFooter.set(player, header, footer);
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true; // TODO?
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
