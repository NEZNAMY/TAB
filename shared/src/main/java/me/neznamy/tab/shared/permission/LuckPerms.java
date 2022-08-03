package me.neznamy.tab.shared.permission;

import java.util.Optional;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabConstants;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;

/**
 * LuckPerms hook
 */
public class LuckPerms extends PermissionPlugin {

    private final String UPDATE_MESSAGE = "Upgrade to LuckPerms 5";

    /**
     * Constructs new instance with given parameter
     *
     * @param   version
     *          LuckPerms version
     */
    public LuckPerms(String version) {
        super(version);
    }

    @Override
    public String getPrimaryGroup(TabPlayer p) {
        try {
            if (getVersion().startsWith("4")) return UPDATE_MESSAGE;
            net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
            User user = api.getUserManager().getUser(p.getUniqueId());
            if (user == null) return TabConstants.NO_GROUP; //pretend like nothing is wrong
            return user.getPrimaryGroup();
        } catch (Exception e) {
            return TabConstants.NO_GROUP;
        }
    }

    /**
     * Returns player's prefix configured in LuckPerms
     *
     * @param   p
     *          Player to get prefix of
     * @return  Player's prefix
     */
    public String getPrefix(TabPlayer p) {
        return getValue(p, true);
    }

    /**
     * Returns player's suffix configured in LuckPerms
     *
     * @param   p
     *          Player to get suffix of
     * @return  Player's suffix
     */
    public String getSuffix(TabPlayer p) {
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
    private String getValue(TabPlayer p, boolean prefix) {
        try {
            if (getVersion().startsWith("4")) return UPDATE_MESSAGE;
            net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
            User user = api.getUserManager().getUser(p.getUniqueId());
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
        } catch (Exception e) {
            //pretend like nothing is wrong
            return "";
        }
    }
}