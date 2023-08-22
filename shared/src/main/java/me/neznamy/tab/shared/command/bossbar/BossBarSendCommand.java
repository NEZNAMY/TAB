package me.neznamy.tab.shared.command.bossbar;

import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handler for "/tab bossbar send &lt;name&gt; [player]" subcommand
 */
public class BossBarSendCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public BossBarSendCommand() {
        super("send", TabConstants.Permission.COMMAND_SCOREBOARD_SHOW);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        BossBarManager feature = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
        if (feature == null) {
            sendMessage(sender, getMessages().getBossBarNotEnabled());
            return;
        }
        if (args.length != 3) {
            sendMessage(sender, getMessages().getSendBarCommandUsage());
            return;
        }
        TabPlayer target = TAB.getInstance().getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
            return;
        }
        String barName = args[1];
        int duration;
        try {
            duration = Integer.parseInt(args[2]);
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
        feature.sendBossBarTemporarily(target, bar.getName(), duration);
        sendMessage(sender, getMessages().getBossBarSendSuccess(target.getName(), bar.getName(), duration));
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        BossBarManager b = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
        if (b == null) return Collections.emptyList();
        if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
        if (arguments.length == 2) return getStartingArgument(b.getRegisteredBossBars().keySet(), arguments[1]);
        if (arguments.length == 3 && b.getBossBar(arguments[1]) != null) return getStartingArgument(Arrays.asList("5", "10", "30", "60", "120"), arguments[2]);
        return Collections.emptyList();
    }
}