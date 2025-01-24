package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.header.HeaderFooter;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Base TabList class for all implementations.
 *
 * @param   <C>
 *          Component class
 */
public abstract class TabListBase<C> extends TrackedTabList<BukkitTabPlayer, C> {

    /** Versions supported by paper module that uses direct mojang-mapped NMS for latest MC version */
    private static final EnumSet<ProtocolVersion> paperNativeVersions = EnumSet.of(
            ProtocolVersion.V1_21_4
    );

    /** Instance function */
    @Getter
    @Setter
    private static FunctionWithException<BukkitTabPlayer, TabListBase<?>> instance;

    @Nullable
    protected static SkinData skinData;

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
     *
     * @param   serverVersion
     *          Server version
     */
    public static void findInstance(@NotNull ProtocolVersion serverVersion) {
        try {
            if (ReflectionUtils.classExists("org.bukkit.craftbukkit.CraftServer") && paperNativeVersions.contains(serverVersion)) {
                Constructor<?> constructor = Class.forName("me.neznamy.tab.platforms.paper.PaperPacketTabList").getConstructor(BukkitTabPlayer.class);
                instance = player -> (TabListBase<?>) constructor.newInstance(player);
            } else if (ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket")) {
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
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        if (HeaderFooter.getInstance() != null) HeaderFooter.getInstance().set(player, header, footer);
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
        return skinData.getSkin(player);
    }
}
