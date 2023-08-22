package me.neznamy.tab.shared.command.bossbar;

import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for "/tab bossbar announce &lt;name&gt; &lt;length&gt;" subcommand
 */
public class BossBarAnnounceCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public BossBarAnnounceCommand() {
        super("announce", TabConstants.Permission.COMMAND_BOSSBAR_ANNOUNCE);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        BossBarManager feature = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
        if (feature == null) {
            sendMessage(sender, getMessages().getBossBarNotEnabled());
            return;
        }
        if (args.length != 2) {
            sendMessage(sender, getMessages().getBossBarAnnounceCommandUsage());
            return;
        }
        String barName = args[0];
        int duration;
        try {
            duration = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendMessage(sender, getMessages().getInvalidNumber(args[1]));
            return;
        }
        BossBar bar = feature.getBossBar(barName);
        if (bar == null) {
            sendMessage(sender, getMessages().getBossBarNotFound(barName));
            return;
        }
        if (!bar.isAnnouncementBar()) {
            sendMessage(sender, getMessages().getBossBarNotMarkedAsAnnouncement());
            return;
        }
        if (feature.getAnnouncedBossBars().contains(bar)) {
            sendMessage(sender, getMessages().getBossBarAlreadyAnnounced());
            return;
        }
        feature.announceBossBar(bar.getName(), duration);
        sendMessage(sender, getMessages().getBossBarAnnouncementSuccess(bar.getName(), duration));
    }

    @Override
    @NotNull
    public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        BossBarManager feature = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
        if (feature == null) return Collections.emptyList();
        List<String> suggestions = new ArrayList<>();
        if (arguments.length == 1) {
            for (String bar : feature.getRegisteredBossBars().values().stream().filter(BossBar::isAnnouncementBar).map(BossBar::getName).collect(Collectors.toList())) {
                if (bar.toLowerCase().startsWith(arguments[0].toLowerCase())) suggestions.add(bar);
            }
        } else if (arguments.length == 2 && feature.getRegisteredBossBars().get(arguments[0]) != null) {
            for (String time : Arrays.asList("5", "10", "30", "60", "120")) {
                if (time.startsWith(arguments[1])) suggestions.add(time);
            }
        }
        return suggestions;
    }
}