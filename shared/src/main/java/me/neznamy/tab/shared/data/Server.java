package me.neznamy.tab.shared.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class storing information about a server. Instances are re-used,
 * so identity comparison is available to see if two servers are the same.
 */
@Getter
public class Server {

    /** Name of the server */
    @NonNull
    private final String name;

    /** Flag tracking whether this server is marked as spy-server in global playerlist configuration or not */
    private boolean isSpyServer;

    /** Server group this server is part of, null if global playerlist is disabled */
    @Nullable
    @Setter
    private ServerGroup serverGroup;

    /**
     * Constructs new instance with given name.
     *
     * @param   name
     *          Name of the server
     */
    Server(@NonNull String name) {
        this.name = name;
        serverGroup = TAB.getInstance().getDataManager().computeServerGroup(this);
    }

    /**
     * Returns server with given name, creating it if it does not exist.
     *
     * @param   name
     *          Name of the server
     * @return  Server instance with given name
     */
    @Contract("!null -> !null")
    public static Server byName(@Nullable String name) {
        if (name == null) return null;
        return TAB.getInstance().getDataManager().getServers().computeIfAbsent(name, Server::new);
    }

    /**
     * Marks this server as spy-server defined in global playerlist configuration.
     */
    public void markSpyServer() {
        isSpyServer = true;
    }

    /**
     * Checks whether this server can see the other server in global playerlist.
     * Spy-servers can see all servers, other servers can only see servers in the same group.
     * If global playerlist is disabled, returns {@code true} if this server is equal to the other one.
     *
     * @param   other
     *          Other server to check visibility to
     * @return  {@code true} if this server can see the other server, {@code false} if not
     */
    public boolean canSee(@NotNull Server other) {
        if (this == other) return true; // same server can always see itself
        if (TAB.getInstance().getConfiguration().getConfig().getGlobalPlayerList() != null) {
            return isSpyServer || serverGroup == other.serverGroup;
        }
        return false;
    }
}
