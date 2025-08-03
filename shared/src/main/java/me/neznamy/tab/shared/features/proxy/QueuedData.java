package me.neznamy.tab.shared.features.proxy;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class holding information about a player connected to another proxy before they joined in.
 * This is caused by messages being received in the wrong order, which cannot be otherwise fixed.
 */
@Getter
@Setter
public class QueuedData {

    /** Belowname number for 1.20.2- */
    private int belowNameNumber;

    /** Belowname NumberFormat for 1.20.3+ */
    @Nullable
    private TabComponent belowNameFancy;

    /** Tablist display name */
    @Nullable
    private TabComponent tabFormat;

    /** Scoreboard team name */
    @Nullable
    private String teamName;

    /** Nametag prefix */
    @Nullable
    private TabComponent tagPrefix;

    /** Nametag suffix */
    @Nullable
    private TabComponent tagSuffix;

    /** Nametag visibility rule */
    @Nullable
    private Scoreboard.NameVisibility nameVisibility;

    /** Playerlist objective number for 1.20.2- */
    private int playerlistNumber;

    /** Playerlist objective NumberFormat for 1.20.3+ */
    @Nullable
    private TabComponent playerlistFancy;

    /** Whether player is vanished or not */
    private boolean vanished;

    /** Name of server the player is connected to */
    @NotNull
    public Server server;
}
