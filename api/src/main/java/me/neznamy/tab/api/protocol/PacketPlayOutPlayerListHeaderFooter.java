package me.neznamy.tab.api.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * A class representing platform specific packet class
 */
@Data @AllArgsConstructor
public class PacketPlayOutPlayerListHeaderFooter implements TabPacket {

    /** TabList header */
    @NonNull private final IChatBaseComponent header;

    /** TabList footer */
    @NonNull private final IChatBaseComponent footer;

    /**
     * Constructs new instance with given parameters. They are converted to {@link IChatBaseComponent}
     * using {@link IChatBaseComponent#optimizedComponent(String)} method.
     * 
     * @param   header
     *          TabList header
     * @param   footer
     *          TabList footer
     */
    public PacketPlayOutPlayerListHeaderFooter(@NonNull String header, @NonNull String footer) {
        this.header = IChatBaseComponent.optimizedComponent(header);
        this.footer = IChatBaseComponent.optimizedComponent(footer);
    }
}