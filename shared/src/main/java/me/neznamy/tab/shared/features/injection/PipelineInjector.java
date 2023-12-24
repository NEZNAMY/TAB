package me.neznamy.tab.shared.features.injection;

import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.TAB;
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

    @Getter private final String featureName = "Pipeline injection";

    //anti-override rules
    protected boolean antiOverrideTeams;
    protected boolean byteBufDeserialization;

    public abstract void inject(TabPlayer player);

    public abstract void uninject(TabPlayer player);

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
}