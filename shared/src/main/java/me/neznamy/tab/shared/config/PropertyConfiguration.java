package me.neznamy.tab.shared.config;

import com.google.common.collect.Lists;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Interface representing user or group configuration.
 * Implementation is either using file or MySQL, if it's enabled.
 */
public interface PropertyConfiguration {

    /** List of all valid properties for groups and users */
    @NotNull
    List<String> VALID_PROPERTIES = Lists.newArrayList("tagprefix", "tagsuffix", "tabprefix", "customtabname", "tabsuffix");

    /**
     * Sets property value of group or user to specified value. If {@code world} or
     * {@code server} is not {@code null}, value will be inserted as a per-world or
     * per-server setting. If both {@code world} and {@code server} are {@code null},
     * value is inserted as a global setting.
     *
     * @param   groupOrUser
     *          Name of group or user, depending on what this instance handles
     * @param   property
     *          Name of property to set
     * @param   server
     *          Server to apply setting to, {@code null} for global setting
     * @param   world
     *          World to apply setting to, {@code null} for global setting
     * @param   value
     *          Value of the property
     */
    void setProperty(@NonNull String groupOrUser, @NonNull String property, @Nullable Server server, @Nullable World world, @Nullable String value);

    /**
     * Gets property of group or user. If {@code server} or {@code world}
     * are not {@code null}, per-server / per-world settings are checked first
     * to try to find a match. If no match is found, global value is returned.
     * Returned value is an array with 2 elements, with first argument being value
     * and second being the source. If nothing is found, empty array is returned.
     *
     * @param   groupOrUser
     *          Name of group or user, depending on what this instance handles
     * @param   property
     *          Name of property to get
     * @param   server
     *          Server the player is currently in, to allow check for per-server settings
     * @param   world
     *          World the player is currently in, to allow check for per-world settings
     * @return  Array with 2 elements with value being first, source second if found,
     *          empty array if nothing was found.
     */
    @NotNull
    String[] getProperty(@NonNull String groupOrUser, @NonNull String property, @Nullable Server server, @Nullable World world);

    /**
     * Removes all data applied to specified group or user.
     *
     * @param   groupOrUser
     *          Name of group or user, depending on what this instance handles
     */
    void remove(@NonNull String groupOrUser);

    /**
     * Returns map of global settings applied to specified group or user.
     * Map key is property name, value is property value.
     *
     * @param   groupOrUser
     *          Name of group or user, depending on what this instance handles
     * @return  Map of global settings of specified group or user
     */
    @NotNull
    Map<String, Object> getGlobalSettings(@NonNull String groupOrUser);

    /**
     * Returns map of per-world settings of specified group or user.
     * Map key is world name, value is property name-value map in that world.
     *
     * @param   groupOrUser
     *          Name of group or user, depending on what this instance handles
     * @return  Map of per-world settings of specified group or user
     */
    @NotNull
    Map<String, Map<String, Object>> getPerWorldSettings(@NonNull String groupOrUser);

    /**
     * Returns map of per-server settings of specified group or user.
     * Map key is server name, value is property name-value map in that server.
     *
     * @param   groupOrUser
     *          Name of group or user, depending on what this instance handles
     * @return  Map of per-server settings of specified group or user
     */
    @NotNull
    Map<String, Map<String, Object>> getPerServerSettings(@NonNull String groupOrUser);

    /**
     * Returns set of all groups or users that have anything configured,
     * either globally or in some world or server.
     *
     * @return  Set of all entries with something configured
     */
    @NotNull
    Set<String> getAllEntries();

    /**
     * Converts per-world or per-server map into a per-world or per-server map
     * with values only belonging to entered group or user. In other words, extracts
     * settings belonging to group or user from the map.
     *
     * @param   map
     *          Original per-world or per-server setting map
     * @param   groupOrUser
     *          Name of group or user, depending on what this instance handles
     * @return  Converted map only containing data of specified group or user
     */
    @NotNull
    default Map<String, Map<String, Object>> convertMap(@NonNull Map<String, Map<String, Map<String, Object>>> map, String groupOrUser) {
        Map<String, Map<String, Object>> converted = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, Object>>> entry : map.entrySet()) {
            converted.put(entry.getKey(), entry.getValue().get(groupOrUser));
        }
        return converted;
    }

    /**
     * Converts object into string. For most objects, {@link Object#toString()} is called.
     * For lists, entries are connected with {@code "\n"}.
     *
     * @param   obj
     *          Object to convert
     * @return  Converted string
     */
    @SuppressWarnings("unchecked")
    @NotNull
    default String toString(@NonNull Object obj) {
        if (obj instanceof List) {
            return ((List<Object>)obj).stream().map(Object::toString).collect(Collectors.joining("\n"));
        }
        return obj.toString();
    }

    /**
     * Converts string into object. If string contains {@code "\n"},
     * a list is created using {@code "\n"} as a separator. Otherwise,
     * inserted string is returned.
     *
     * @param   string
     *          String to convert
     * @return  Converted object
     */
    @Nullable
    default Object fromString(@Nullable String string) {
        if (string != null && string.contains("\n")) {
            return Arrays.asList(string.split("\n"));
        }
        return string;
    }

    /**
     * Checks if configured property name is valid. If not, prints a warning.
     *
     * @param   source
     *          Source of the configuration (specific file or MySQL)
     * @param   type
     *          Entity type (group / player)
     * @param   name
     *          Name of the group or player
     * @param   property
     *          Configured property name
     * @param   server
     *          Server if this property is only in specified server
     * @param   world
     *          World if this property is only in specified world
     * @param   startupWarn
     *          Whether the message should be printed as startup warn and counted or not
     */
    default void checkProperty(@NonNull String source, @NonNull String type, @NonNull String name, @NonNull String property,
                               @Nullable String server, @Nullable String world, boolean startupWarn) {
        if (VALID_PROPERTIES.contains(property.toLowerCase(Locale.US))) return;
        StringBuilder msg = new StringBuilder(String.format("[%s] Unknown property \"%s\" defined for %s \"%s\"", source, property, type, name));
        if (world != null) msg.append(" in world \"").append(world).append("\"");
        if (server != null) msg.append(" in server \"").append(server).append("\"");
        msg.append(". Valid properties: ").append(VALID_PROPERTIES);
        if (startupWarn) {
            TAB.getInstance().getConfigHelper().startup().startupWarn(msg.toString());
        } else {
            TAB.getInstance().getConfigHelper().runtime().error(msg.toString());
        }
    }
}