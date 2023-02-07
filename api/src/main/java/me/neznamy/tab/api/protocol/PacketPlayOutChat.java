package me.neznamy.tab.api.protocol;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * A class representing platform specific packet class
 */
@Data
public class PacketPlayOutChat implements TabPacket {

    /** Message to be sent */
    @NonNull private final IChatBaseComponent message;

    /** Message position */
    @NonNull private final ChatMessageType type;

    /**
     * An enum representing positions of a chat message
     * Calling ordinal() will return type's network ID.
     */
    public enum ChatMessageType {

        CHAT,
        SYSTEM,
        GAME_INFO
    }
}