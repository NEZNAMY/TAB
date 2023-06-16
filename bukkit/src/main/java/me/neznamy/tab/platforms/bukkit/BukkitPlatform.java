package me.neznamy.tab.platforms.bukkit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.platforms.bukkit.features.BukkitTabExpansion;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.platforms.bukkit.features.WitherBossBar;
import me.neznamy.tab.platforms.bukkit.features.BukkitNameTagX;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Implementation of Platform interface for Bukkit platform
 */
@RequiredArgsConstructor
@Getter
public class BukkitPlatform implements BackendPlatform {

    /** Plugin instance for registering tasks and events */
    private final JavaPlugin plugin;

    /** Variables checking presence of other plugins to hook into */
    private final boolean placeholderAPI = ReflectionUtils.classExists("me.clip.placeholderapi.PlaceholderAPI");
    @Setter private boolean libsDisguisesEnabled = ReflectionUtils.classExists("me.libraryaddict.disguise.DisguiseAPI");

    public @NotNull BossBarManagerImpl getLegacyBossBar() {
        return new WitherBossBar(plugin);
    }

    @Override
    public void loadPlayers() {
        for (Player p : getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new BukkitTabPlayer(p));
        }
    }

    @Override
    public void registerPlaceholders() {
        new BukkitPlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public @Nullable PipelineInjector createPipelineInjector() {
        return NMSStorage.getInstance().getMinorVersion() >= 8 ? new BukkitPipelineInjector() : null;
    }

    @Override
    public @NotNull NameTag getUnlimitedNameTags() {
        return new BukkitNameTagX(plugin);
    }

    @Override
    public @NotNull TabExpansion createTabExpansion() {
        if (placeholderAPI) {
            BukkitTabExpansion expansion = new BukkitTabExpansion();
            expansion.register();
            return expansion;
        }
        return new EmptyTabExpansion();
    }

    @Override
    public @Nullable TabFeature getPerWorldPlayerList() {
        return new PerWorldPlayerList(plugin);
    }

    /**
     * Returns online players from Bukkit API
     *
     * @return  online players from Bukkit API
     */
    @SuppressWarnings("unchecked")
    private @NotNull Player[] getOnlinePlayers() {
        try {
            Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
            if (players instanceof Player[]) {
                //1.7-
                return (Player[]) players;
            } else {
                //1.8+
                return ((Collection<Player>)players).toArray(new Player[0]); 
            }
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get online players", e);
            return new Player[0];
        }
    }

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
        int refresh = pl.getRefreshInterval(identifier);
        if (identifier.startsWith("%rel_")) {
            //relational placeholder
            TAB.getInstance().getPlaceholderManager().registerRelationalPlaceholder(identifier, pl.getRefreshInterval(identifier), (viewer, target) ->
                    placeholderAPI ? PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier) : identifier);
        } else if (identifier.startsWith("%sync:")) {
            String syncedPlaceholder = "%" + identifier.substring(6, identifier.length()-1) + "%";
            PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
            ppl[0] = pl.registerPlayerPlaceholder(identifier, refresh, p -> {
                try {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        long time = System.nanoTime();
                        ppl[0].updateValue(p, placeholderAPI ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), syncedPlaceholder) : identifier);
                        TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, System.nanoTime()-time);
                    });
                    return null;
                } catch (UnsupportedOperationException e) {
                    return "<Folia does not support sync placeholders>";
                }
            });
        } else if (identifier.startsWith("%server_")) {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, refresh, () ->
                    placeholderAPI ? PlaceholderAPI.setPlaceholders(null, identifier) : identifier);
        } else {
            TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, refresh, p ->
                    placeholderAPI ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), identifier) : identifier);
        }
    }

    @Override
    public void sendConsoleMessage(@NotNull IChatBaseComponent message) {
        Bukkit.getConsoleSender().sendMessage("[TAB] " + RGBUtils.getInstance().convertToBukkitFormat(message.toFlatText(),
                TAB.getInstance().getServerVersion().getMinorVersion() >= 16));
    }

    @Override
    public String getServerVersionInfo() {
        return "[Bukkit] " + Bukkit.getName() + " - " + Bukkit.getBukkitVersion().split("-")[0] + " (" + NMSStorage.getInstance().getServerPackage() + ")";
    }
}