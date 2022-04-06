package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;

import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import net.milkbowl.vault.chat.Chat;

/**
 * Bukkit registry to register bukkit-only and universal placeholders
 */
public class BukkitPlaceholderRegistry extends UniversalPlaceholderRegistry {

    /** Number formatter for 2 decimal places */
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    private Object chat; // Object because Fabric
    private final Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
    private Object server;
    private Field recentTps;
    private Method paperTps;
    private Method paperMspt;
    private Method purpurIsAfk;

    /**
     * Constructs new instance with given parameter
     */
    public BukkitPlaceholderRegistry() {
        numberFormat.setMaximumFractionDigits(2);
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<?> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
            if (rspChat != null) chat = rspChat.getProvider();
        }
        try {
            server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            recentTps = server.getClass().getField("recentTps");
        } catch (ReflectiveOperationException e) {
            //not spigot
        }
        try { paperTps = Bukkit.class.getMethod("getTPS"); } catch (NoSuchMethodException ignored) {}
        try { paperMspt = Bukkit.class.getMethod("getAverageTickTime"); } catch (NoSuchMethodException ignored) {}
        try { purpurIsAfk = Player.class.getMethod("isAfk"); } catch (NoSuchMethodException ignored) {}
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerPlaceholders(PlaceholderManager manager) {
        super.registerPlaceholders(manager);
        manager.registerPlayerPlaceholder("%displayname%", 500, p -> ((Player) p.getPlayer()).getDisplayName());
        if (paperTps != null) {
            manager.registerServerPlaceholder("%tps%", 1000, () -> formatTPS(Bukkit.getTPS()[0]));
        } else if (recentTps != null) {
            manager.registerServerPlaceholder("%tps%", 1000, () -> {
                try {
                    return formatTPS(((double[]) recentTps.get(server))[0]);
                } catch (IllegalAccessException e) {
                    return "-1";
                }
            });
        } else {
            manager.registerServerPlaceholder("%tps%", -1, () -> "-1");
        }
        if (paperMspt != null) {
            manager.registerServerPlaceholder("%mspt%", 1000, () -> numberFormat.format(Bukkit.getAverageTickTime()));
        }
        manager.registerPlayerPlaceholder("%afk%", 500, p -> {
            if (essentials != null && ((Essentials)essentials).getUser(p.getUniqueId()).isAfk()) return true;
            return purpurIsAfk != null && ((Player)p.getPlayer()).isAfk();
        });
        manager.registerPlayerPlaceholder("%essentialsnick%", 1000, p -> {
            String nickname = null;
            if (essentials != null)
                nickname = ((Essentials)essentials).getUser(p.getUniqueId()).getNickname();
            return nickname == null || nickname.length() == 0 ? p.getName() : nickname;
        });
        if (chat != null) {
            manager.registerPlayerPlaceholder("%vault-prefix%", 1000, p -> ((Chat) chat).getPlayerPrefix((Player) p.getPlayer()));
            manager.registerPlayerPlaceholder("%vault-suffix%", 1000, p -> ((Chat) chat).getPlayerSuffix((Player) p.getPlayer()));
        } else {
            manager.registerServerPlaceholder("%vault-prefix%", -1, () -> "");
            manager.registerServerPlaceholder("%vault-suffix%", -1, () -> "");
        }
        manager.registerPlayerPlaceholder("%health%", 100, p -> (int) Math.ceil(((Player) p.getPlayer()).getHealth()));
    }

    private String formatTPS(double tps) {
        return numberFormat.format(Math.min(20, tps));
    }
}
