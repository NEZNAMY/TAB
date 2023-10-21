package me.neznamy.tab.shared.hook;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * Class that hooks into LuckPerms if installed.
 */
public class LuckPermsHook {

    /** Instance of the class */
    @Getter private static final LuckPermsHook instance = new LuckPermsHook();

    /** Flag tracking if LuckPerms is installed or not */
    @Getter private final boolean installed = ReflectionUtils.classExists("net.luckperms.api.LuckPerms");

    /** Function retrieving group of player from LuckPerms */
    @Getter private final Function<TabPlayer, String> groupFunction = p -> {
        User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
        if (user == null) return TabConstants.NO_GROUP;

        try {
            File folder = TAB.getInstance().getDataFolder();
            ConfigurationFile groups = null;
            groups = new YamlConfigurationFile(null, new File(folder, "groups.yml"));
            Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
            for (Group group : inheritedGroups) {
                for (Object groupName : groups.getConfigurationSection("custom-groups").keySet()) {
                    if (!groupName.toString().equalsIgnoreCase(group.getName()))
                        continue;
                    String[] serverList = groups.getString("custom-groups." + groupName).toLowerCase().split(",");
                    if (Arrays.toString(serverList).contains("global")) {
                        return groupName.toString();
                    }
                    for (String server : serverList) {
                        if (server.equalsIgnoreCase(p.getServer())) {
                            return groupName.toString();
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return user.getPrimaryGroup();
    };

    /**
     * Returns player's prefix configured in LuckPerms
     *
     * @param   p
     *          Player to get prefix of
     * @return  Player's prefix
     */
    public String getPrefix(@NonNull TabPlayer p) {
        return getValue(p, true);
    }

    /**
     * Returns player's suffix configured in LuckPerms
     *
     * @param   p
     *          Player to get suffix of
     * @return  Player's suffix
     */
    public String getSuffix(@NonNull TabPlayer p) {
        return getValue(p, false);
    }

    /**
     * Returns player's metadata value based on entered boolean flag,
     * {@code true} for prefix, {@code false} for suffix.
     *
     * @param   p
     *          Player to get metadata value of
     * @param   prefix
     *          {@code true} if prefix should be returned, {@code false} if suffix
     * @return  Player's metadata value
     */
    private String getValue(@NonNull TabPlayer p, boolean prefix) {
        User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
        if (user == null) return "";
        Optional<QueryOptions> options = LuckPermsProvider.get().getContextManager().getQueryOptions(user);
        if (!options.isPresent()) return "";
        CachedMetaData data = user.getCachedData().getMetaData(options.get());
        String value;
        if (prefix) {
            value = data.getPrefix();
        } else {
            value = data.getSuffix();
        }
        return value == null ? "" : value;
    }
}
