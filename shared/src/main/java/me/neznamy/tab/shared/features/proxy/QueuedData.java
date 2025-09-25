package me.neznamy.tab.shared.features.proxy;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.belowname.BelowNameProxyPlayerData;
import me.neznamy.tab.shared.features.nametags.NameTagProxyPlayerData;
import me.neznamy.tab.shared.features.playerlist.PlayerListProxyPlayerData;
import me.neznamy.tab.shared.features.playerlistobjective.PlayerListObjectiveProxyPlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class holding information about a player connected to another proxy before they joined in.
 * This is caused by messages being received in the wrong order, which cannot be otherwise fixed.
 */
@Getter
@Setter
public class QueuedData {

    /** Belowname data */
    @Nullable
    private BelowNameProxyPlayerData belowname;

    /** Playerlist objective data */
    @Nullable
    private PlayerListObjectiveProxyPlayerData playerlist;

    /** Tablist display name */
    @Nullable
    private PlayerListProxyPlayerData tabFormat;

    /** Nametag data */
    @Nullable
    private NameTagProxyPlayerData nametag;

    /** Whether player is vanished or not */
    private boolean vanished;

    /** Name of server the player is connected to */
    @NotNull
    public Server server;
}
