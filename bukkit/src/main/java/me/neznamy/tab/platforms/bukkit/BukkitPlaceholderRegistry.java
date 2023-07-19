package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.backend.BackendPlaceholderRegistry;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit registry to register bukkit-only and universal placeholders
 */
public class BukkitPlaceholderRegistry extends BackendPlaceholderRegistry {

    private Chat chat;

    /**
     * Constructs new instance and loads hooks
     */
    public BukkitPlaceholderRegistry(BukkitPlatform platform) {
        super(platform);
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
            if (rspChat != null) chat = rspChat.getProvider();
        }
    }

    @Override
    public void registerPlaceholders(@NotNull PlaceholderManager manager) {
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
