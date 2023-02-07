package me.neznamy.tab.api.protocol;

import lombok.Data;
import lombok.NonNull;

/**
 * A class representing platform specific packet class
 */
@Data
public class PacketPlayOutScoreboardDisplayObjective implements TabPacket {

    /**
     * Display slot.
     * 0 = PlayerList,
     * 1 = SideBar,
     * 2 = BelowName.
     */
    private final int slot;

    /** Up to 16 characters long objective name */
    @NonNull private final String objectiveName;
}