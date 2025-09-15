package me.neznamy.tab.shared.features.header;

import me.neznamy.tab.shared.Property;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Class holding header/footer data for players.
 */
public class HeaderFooterPlayerData {

    /** Forced header using the API */
    @Nullable
    public Property forcedHeader;

    /** Forced footer using the API */
    @Nullable
    public Property forcedFooter;

    /** Currently active design */
    @Nullable
    public HeaderFooterDesign activeDesign;

    /** Map of header properties for each design */
    public final Map<HeaderFooterDesign, Property> headerProperties = new IdentityHashMap<>();

    /** Map of footer properties for each design */
    public final Map<HeaderFooterDesign, Property> footerProperties = new IdentityHashMap<>();
}