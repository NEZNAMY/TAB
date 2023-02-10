package me.neznamy.tab.platforms.sponge;

import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;

public final class SpongePlatform extends Platform {

    private final Main plugin;

    public SpongePlatform(final Main plugin) {
        super(new PacketBuilder());
        this.plugin = plugin;
    }

    @Override
    public PermissionPlugin detectPermissionPlugin() {
        if (Sponge.getPluginManager().isLoaded("luckperms")) {
            return new LuckPerms(getPluginVersion("luckperms"));
        }
        return new None();
    }

    @Override
    public void loadFeatures() {
        final TAB tab = TAB.getInstance();
        new SpongePlaceholderRegistry().registerPlaceholders(tab.getPlaceholderManager());
        if (tab.getConfiguration().getConfig().getBoolean("scoreboard-teams.enabled", true)) {
            tab.getFeatureManager().registerFeature(TabConstants.Feature.SORTING, new Sorting());
            tab.getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS, new NameTag());
        }

        tab.loadUniversalFeatures();
        if (tab.getConfiguration().getConfig().getBoolean("bossbar.enabled", false)) {
            tab.getFeatureManager().registerFeature(TabConstants.Feature.BOSS_BAR, new BossBarManagerImpl());
        }

        for (final Player player : Sponge.getServer().getOnlinePlayers()) {
            tab.addPlayer(new SpongeTabPlayer(player));
        }
    }

    @Override
    public String getPluginVersion(final String plugin) {
        return Sponge.getPluginManager().getPlugin(plugin).flatMap(PluginContainer::getVersion).orElse(null);
    }

    @Override
    public void registerUnknownPlaceholder(final String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }
}
