package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Locale;

import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import net.ess3.api.events.NickChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;

import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit registry to register bukkit-only and universal placeholders
 */
public class BukkitPlaceholderRegistry extends UniversalPlaceholderRegistry {

    /** Number formatter for 2 decimal places */
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    /** JavaPlugin reference for registering events */
    private final JavaPlugin plugin;

    /** Vault Chat hook */
    private Chat chat;

    /** NMS server to get TPS from on spigot */
    private Object server;

    /** TPS field*/
    private Field recentTps;

    /** Detection for presence of Paper's TPS getter */
    private Method paperTps;

    /** Detection for presence of Paper's MSPT getter */
    private Method paperMspt;

    /** Detection for presence of Purpur's AFK getter */
    private Method purpurIsAfk;

    /**
     * Constructs new instance with given parameter and loads hooks
     *
     * @param   plugin
     *          reference to the plugin's main class
     */
    public BukkitPlaceholderRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        numberFormat.setMaximumFractionDigits(2);
        if (Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.VAULT)) {
            RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
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
            manager.registerServerPlaceholder(TabConstants.Placeholder.MSPT, 1000, () -> numberFormat.format(Bukkit.getAverageTickTime()));
        }
        Plugin essentials = Bukkit.getPluginManager().getPlugin(TabConstants.Plugin.ESSENTIALS);
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.AFK, 500, p -> {
            if (essentials != null && ((Essentials)essentials).getUser(p.getUniqueId()).isAfk()) return true;
            return purpurIsAfk != null && ((Player)p.getPlayer()).isAfk();
        });
        if (essentials != null) {
            PlayerPlaceholder nick = manager.registerPlayerPlaceholder(TabConstants.Placeholder.ESSENTIALS_NICK, -1, p -> {
                String nickname = ((Essentials)essentials).getUser(p.getUniqueId()).getNickname();
                return nickname == null ? p.getName() : nickname;
            });
            Listener nickListener = new Listener() {
                @EventHandler
                public void onNickChange(NickChangeEvent e) {
                    String name = e.getValue() == null ? e.getController().getName() : e.getValue();
                    TabPlayer player = TAB.getInstance().getPlayer(e.getController().getUUID());
                    if (player == null) return;
                    nick.updateValue(player, name);
                }
            };
            nick.enableTriggerMode(() -> Bukkit.getPluginManager().registerEvents(nickListener, plugin),
                    () -> HandlerList.unregisterAll(nickListener));
        } else {
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.ESSENTIALS_NICK, -1, TabPlayer::getName);
        }

        if (chat != null) {
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.VAULT_PREFIX, 1000, p -> chat.getPlayerPrefix((Player) p.getPlayer()));
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.VAULT_SUFFIX, 1000, p -> chat.getPlayerSuffix((Player) p.getPlayer()));
        } else {
            manager.registerServerPlaceholder(TabConstants.Placeholder.VAULT_PREFIX, -1, () -> "");
            manager.registerServerPlaceholder(TabConstants.Placeholder.VAULT_SUFFIX, -1, () -> "");
        }
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100, p -> (int) Math.ceil(((Player) p.getPlayer()).getHealth()));
        super.registerPlaceholders(manager);
    }

    /**
     * Formats TPS using number formatter with 2 decimal places.
     *
     * @param   tps
     *          TPS to format
     * @return  Formatted TPS as a String
     */
    private String formatTPS(double tps) {
        return numberFormat.format(Math.min(20, tps));
    }
}
