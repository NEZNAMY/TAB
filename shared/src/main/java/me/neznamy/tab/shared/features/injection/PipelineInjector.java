package me.neznamy.tab.shared.features.injection;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Packet intercepting to secure proper functionality of some features:
 * TabList names - anti-override
 * NameTags - anti-override
 * Scoreboard - disabling tab's scoreboard to prevent conflict
 * PingSpoof - full feature functionality
 * Unlimited name tags - replacement for bukkit events with much better accuracy and reliability
 * NickCompatibility - Detect name changes from other plugins
 */
public abstract class PipelineInjector extends TabFeature implements JoinListener, Loadable, UnLoadable {

    /** Team anti-override flag */
    protected boolean antiOverrideTeams;

    /** Whether ByteBuf deserialization should be enabled or not */
    protected boolean byteBufDeserialization;

    /**
     * Injects handler into player's channel.
     *
     * @param   player
     *          Player to inject
     */
    public abstract void inject(@NotNull TabPlayer player);

    /**
     * Un-injects handler from player's channel.
     *
     * @param   player
     *          Player to remove handler from
     */
    public abstract void uninject(@NotNull TabPlayer player);

    @Override
    public void load() {
        antiOverrideTeams = config().getBoolean("scoreboard-teams.enabled", true) &&
                config().getBoolean("scoreboard-teams.anti-override", true);
        boolean respectOtherScoreboardPlugins = config().getBoolean("scoreboard.enabled", false) &&
                config().getBoolean("scoreboard.respect-other-plugins", true);
        byteBufDeserialization = antiOverrideTeams || respectOtherScoreboardPlugins;
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            inject(p);
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            uninject(p);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        inject(connectedPlayer);
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return "Pipeline injection";
    }
}