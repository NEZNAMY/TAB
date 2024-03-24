package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.NameTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetCollisionCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public SetCollisionCommand() {
        super("setcollision", TabConstants.Permission.COMMAND_SETCOLLISION);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        NameTag feature = TAB.getInstance().getNameTagManager();
        if (feature == null) {
            sendMessage(sender, getMessages().getTeamFeatureRequired());
            return;
        }
        if (args.length == 2) {
            TabPlayer target = TAB.getInstance().getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
                return;
            }
            feature.setCollisionRule(target, Boolean.parseBoolean(args[1]));
            feature.updateTeamData(target);
        } else {
            sendMessage(sender, getMessages().getCollisionCommandUsage());
        }
    }
    
    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
        if (arguments.length == 2) return getStartingArgument(Arrays.asList("true", "false"), arguments[1]);
        return Collections.emptyList();
    }
}