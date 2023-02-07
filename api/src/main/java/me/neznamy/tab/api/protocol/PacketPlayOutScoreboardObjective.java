package me.neznamy.tab.api.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.util.Preconditions;

/**
 * A class representing platform specific packet class
 */
@Data @AllArgsConstructor
public class PacketPlayOutScoreboardObjective implements TabPacket {

    /**
     * Packet action.
     * 0 = register,
     * 1 = unregister,
     * 2 = update title
     */
    private final int action;

    /** Up to 16 characters long objective name */
    @NonNull private final String objectiveName;

    /** Display name */
    private final String displayName;

    /** Display type, only takes effect in PlayerList */
    private final EnumScoreboardHealthDisplay renderType;

    /**
     * Constructs new packet with given objective name and 1 (unregister) action.
     *
     * @param   objectiveName
     *          objective name, up to 16 characters long
     * @throws  IllegalArgumentException
     *          if {@code objectiveName} is null or longer than 16 characters
     */
    public PacketPlayOutScoreboardObjective(@NonNull String objectiveName) {
        Preconditions.checkMaxLength(objectiveName, 16, "objective name");
        this.objectiveName = objectiveName;
        this.displayName = ""; //avoiding NPE on <1.7
        this.action = 1;
        this.renderType = null;
    }

    /**
     * An enum representing available display types.
     * They only take effect in PlayerList position.
     */
    public enum EnumScoreboardHealthDisplay {

        INTEGER, HEARTS
    }
}