package me.neznamy.tab.api.protocol;

import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.util.BiFunctionWithException;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for packet building, methods are overridden in
 * platform-specific builders which extend this class to build actual packets.
 * Packets which don't have a method override are forwarded into
 * sendPacket method, where they are handled using platform's API instead.
 */
public class PacketBuilder {

    /** Function map turning custom packet class into platform-specific packets */
    protected final Map<Class<? extends TabPacket>, BiFunctionWithException<TabPacket, ProtocolVersion, Object>> buildMap = new HashMap<>();

    /**
     * Converts custom packet into platform-specific packet by calling a function from
     * {@link #buildMap}.
     *
     * @param   packet
     *          Packet to build
     * @param   clientVersion
     *          Protocol version of player to build the packet for
     * @return  Platform-specific packet
     * @throws  ReflectiveOperationException
     *          if reflection fails
     */
    public Object build(@NonNull TabPacket packet, @NonNull ProtocolVersion clientVersion) throws ReflectiveOperationException {
        return buildMap.get(packet.getClass()).apply(packet, clientVersion);
    }

    /**
     * Cuts given string to specified character length (or length-1 if last character is a color character)
     * and translates RGB to legacy colors. If string is not that long, the original string is returned.
     * RGB codes are converted into legacy, since cutting is only needed for &lt;1.13.
     * If {@code string} is {@code null}, empty string is returned.
     *
     * @param   string
     *          String to cut
     * @param   length
     *          Length to cut to
     * @return  string cut to {@code length} characters
     */
    public String cutTo(String string, int length) {
        if (string == null) return "";
        String legacyText = string;
        if (string.contains("#")) {
            //converting RGB to legacy colors
            legacyText = RGBUtils.getInstance().convertRGBtoLegacy(string);
        }
        if (legacyText.length() <= length) return legacyText;
        if (legacyText.charAt(length-1) == EnumChatFormat.COLOR_CHAR) {
            return legacyText.substring(0, length-1); //cutting one extra character to prevent prefix ending with "&"
        } else {
            return legacyText.substring(0, length);
        }
    }

    /**
     * If {@code clientVersion} is &gt;= 1.13, creates a component from given text and returns
     * it as a serialized component, which BungeeCord uses.
     * <p>
     * If {@code clientVersion} is &lt; 1.12, the text is cut to {@code length} characters if
     * needed and returned.
     *
     * @param   text
     *          Text to convert
     * @param   clientVersion
     *          Version of player to convert text for
     * @return  serialized component for 1.13+ clients, cut string for 1.12-
     */
    public String jsonOrCut(String text, ProtocolVersion clientVersion, int length) {
        if (text == null) return null;
        if (clientVersion.getMinorVersion() >= 13) {
            return IChatBaseComponent.optimizedComponent(text).toString(clientVersion);
        } else {
            return cutTo(text, length);
        }
    }
}