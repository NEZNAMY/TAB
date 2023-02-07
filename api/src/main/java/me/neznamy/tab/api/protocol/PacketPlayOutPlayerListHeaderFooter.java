package me.neznamy.tab.api.protocol;

import lombok.*;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.util.Preconditions;

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
    public PacketPlayOutPlayerListHeaderFooter(String header, String footer) {
        Preconditions.checkNotNull(header, "header");
        Preconditions.checkNotNull(footer, "footer");
        this.header = IChatBaseComponent.optimizedComponent(header);
        this.footer = IChatBaseComponent.optimizedComponent(footer);
    }
}