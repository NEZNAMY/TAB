package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Bukkit registry to register bukkit-only and universal placeholders
 */
public class BukkitPlaceholderRegistry extends UniversalPlaceholderRegistry {

    private Chat chat;

    /** NMS server to get TPS from on spigot */
    private Object server;

    /** TPS field*/
    private Field recentTps;

    /** Detection for presence of Paper's TPS getter */
    private Method paperTps;

    /** Detection for presence of Paper's MSPT getter */
    private Method paperMspt;

    /**
     * Constructs new instance and loads hooks
     */
    public BukkitPlaceholderRegistry() {
        try {
            server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            recentTps = server.getClass().getField("recentTps");
        } catch (ReflectiveOperationException e) {
            //not spigot
        }
        try { paperTps = Bukkit.class.getMethod("getTPS"); } catch (NoSuchMethodException ignored) {}
        try { paperMspt = Bukkit.class.getMethod("getAverageTickTime"); } catch (NoSuchMethodException ignored) {}
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
            if (rspChat != null) chat = rspChat.getProvider();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerPlaceholders(@NotNull PlaceholderManager manager) {
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.DISPLAY_NAME, 500, p -> ((Player) p.getPlayer()).getDisplayName());
        if (paperTps != null) {
            manager.registerServerPlaceholder(TabConstants.Placeholder.TPS, 1000, () -> formatTPS(Bukkit.getTPS()[0]));
        } else if (recentTps != null) {
            manager.registerServerPlaceholder(TabConstants.Placeholder.TPS, 1000, () -> {
                try {
                    return formatTPS(((double[]) recentTps.get(server))[0]);
                } catch (IllegalAccessException e) {
                    return -1;
                }
            });
        } else {
            manager.registerServerPlaceholder(TabConstants.Placeholder.TPS, -1, () -> -1);
        }
        if (paperMspt != null) {
            manager.registerServerPlaceholder(TabConstants.Placeholder.MSPT, 1000, () -> format(Bukkit.getAverageTickTime()));
        }
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100, p -> (int) Math.ceil(((Player) p.getPlayer()).getHealth()));
        if (chat != null) {
            manager.registerPlayerPlaceholder("%vault-prefix%", 1000, p -> chat.getPlayerPrefix((Player) p.getPlayer()));
            manager.registerPlayerPlaceholder("%vault-suffix%", 1000, p -> chat.getPlayerSuffix((Player) p.getPlayer()));
        } else {
            manager.registerServerPlaceholder("%vault-prefix%", -1, () -> "");
            manager.registerServerPlaceholder("%vault-suffix%", -1, () -> "");
        }
        // PAPI override to prevent errors when ping field changes and expansion is not updated
        manager.registerPlayerPlaceholder("%player_ping%", ((PlaceholderManagerImpl)manager).getRefreshInterval("%player_ping%"),
                p -> ((TabPlayer)p).getPing());
        super.registerPlaceholders(manager);
    }
}
