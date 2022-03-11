package me.neznamy.tab.api.protocol;

import me.neznamy.tab.api.util.Preconditions;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardDisplayObjective implements TabPacket {

    /**
     * Display slot.
     * 0 = PlayerList,
     * 1 = SideBar,
     * 2 = BelowName.
     */
    private final int slot;

    /** Up to 16 characters long objective name */
    private final String objectiveName;

    /**
     * Constructs new instance with given parameters
     * @param    slot
     *             Display slot
     * @param    objectiveName
     *             Internal name of the objective
     * @throws    IllegalArgumentException
     *             if {@code objectiveName} is null or longer than 16 characters
     */
    public PacketPlayOutScoreboardDisplayObjective(int slot, String objectiveName) {
        Preconditions.checkNotNull(objectiveName, "objective name");
        Preconditions.checkMaxLength(objectiveName, 16, "objective name");
        this.slot = slot;
        this.objectiveName = objectiveName;
    }

    @Override
    public String toString() {
        return String.format("PacketPlayOutScoreboardDisplayObjective{slot=%s,objectiveName=%s}", slot, objectiveName);
    }

    /**
     * Returns {@link #slot}
     * @return    slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Returns {@link #objectiveName}
     * @return    objectiveName
     */
    public String getObjectiveName() {
        return objectiveName;
    }
}