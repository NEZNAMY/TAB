package me.neznamy.tab.shared.command.bossbar;

import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Handler for "/tab bossbar on [player] [options]" subcommand
 */
public class BossBarOnCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public BossBarOnCommand() {
        super("on", TabConstants.Permission.COMMAND_BOSSBAR_TOGGLE);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        BossBarManager feature = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
        if (feature == null) {
            sendMessage(sender, getMessages().getBossBarNotEnabled());
            return;
        }
        TabPlayer target = sender;
        if (args.length > 0) {
            if (hasPermission(sender, TabConstants.Permission.COMMAND_BOSSBAR_TOGGLE_OTHER)) {
                target = TAB.getInstance().getPlayer(args[0]);
                if (target == null) {
                    sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
                    return;
                }
            } else {
                sendMessage(sender, getMessages().getNoPermission());
                return;
            }
        } else if (target == null) {
            sendMessage(null, getMessages().getCommandOnlyFromGame());
            return;
        }
        boolean silent = args.length == 2 && args[1].equals("-s");
        feature.setBossBarVisible(target, true, !silent);
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
        if (arguments.length == 2) return getStartingArgument(Collections.singletonList("-s"), arguments[1]);
        return Collections.emptyList();
    }
}