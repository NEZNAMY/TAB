package me.neznamy.tab.shared.features.alignedplayerlist;

import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class PlayerView {

    @NotNull private final AlignedPlayerList feature;
    @NotNull private final TabPlayer viewer;
    private final boolean canSeeVanished;
    private final Map<TabPlayer, Integer> playerWidths = new WeakHashMap<>();

    private int maxWidth;
    private TabPlayer maxPlayer;

    public PlayerView(@NotNull AlignedPlayerList feature, @NotNull TabPlayer viewer) {
        this.feature = feature;
        this.viewer = viewer;
        canSeeVanished = viewer.hasPermission(TabConstants.Permission.SEE_VANISHED);
    }

    public void load() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            playerWidths.put(all, getPlayerNameWidth(all));
        }
        recalculateMaxWidth(null);
        if (viewer.getVersion().getMinorVersion() < 8) return;
        updateAllPlayers();
    }

    public void playerJoin(@NotNull TabPlayer connectedPlayer) {
        if (viewer.getVersion().getMinorVersion() < 8) return;
        int width = getPlayerNameWidth(connectedPlayer);
        playerWidths.put(connectedPlayer, width);
        if (width > maxWidth && (!connectedPlayer.isVanished() || canSeeVanished)) {
            maxWidth = width;
            maxPlayer = connectedPlayer;
            updateAllPlayers();
        } else {
            viewer.getTabList().updateDisplayName(feature.getTablistUUID(connectedPlayer, viewer), formatName(connectedPlayer));
        }
    }

    private void updateAllPlayers() {
        if (viewer.getVersion().getMinorVersion() < 8) return;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (feature.getDisableChecker().isDisabledPlayer(all)) continue;
            viewer.getTabList().updateDisplayName(feature.getTablistUUID(all, viewer), formatName(all));
        }
    }

    public synchronized IChatBaseComponent formatName(@NotNull TabPlayer target) {
        if (viewer.getVersion().getMinorVersion() < 8) return null;
        Integer width = playerWidths.get(target);
        if (width == null) return null; //in packet reader, not loaded yet, will send packet after loading player
        Property prefixPr = target.getProperty(TabConstants.Property.TABPREFIX);
        Property namePr = target.getProperty(TabConstants.Property.CUSTOMTABNAME);
        Property suffixPr = target.getProperty(TabConstants.Property.TABSUFFIX);
        if (prefixPr == null || namePr == null || suffixPr == null) return null; // no idea why is another check needed
        String prefixAndName = prefixPr.getFormat(viewer) + namePr.getFormat(viewer);
        String suffix = suffixPr.getFormat(viewer);
        if (suffix.length() == 0) return IChatBaseComponent.optimizedComponent(prefixAndName);
        if ((target.isVanished() && !canSeeVanished) || width > maxWidth) {
            //tab sending packets for vanished players or player just unvanished
            return IChatBaseComponent.optimizedComponent(prefixAndName + suffix);
        }
        StringBuilder newFormat = new StringBuilder(prefixAndName).append(EnumChatFormat.RESET.getFormat());
        int length = maxWidth + 12 - width;
        try {
            newFormat.append(buildSpaces(length));
        } catch (IllegalArgumentException e) {
            TAB.getInstance().getErrorManager().printError("Could not build space consisting of " + length + " pixels", e);
        }
        return IChatBaseComponent.optimizedComponent(newFormat.append(EnumChatFormat.getLastColors(prefixAndName)).append(suffix).toString());
    }

    /**
     * Returns a combination of normal and bold spaces to build exactly the requested amount of pixels.
     * Must be at least 12 as lower numbers cannot always be built using numbers 4 (normal space + 1 pixel) and 5 (bold space + 1 pixel)
     * Returns the result string with normal then bold spaces, such as "   &amp;l   &amp;r"
     *
     * @param   pixelWidth
     *          amount of pixels to be built
     * @return  string consisting of spaces and &amp;l &amp;r
     * @throws  IllegalArgumentException
     *          if pixelWidth is &lt; 12
     */
    private String buildSpaces(int pixelWidth) {
        if (pixelWidth < 12) throw new IllegalArgumentException("Cannot build space lower than 12 pixels wide");
        int pixelsLeft = pixelWidth;
        StringBuilder output = new StringBuilder();
        while (pixelsLeft % 5 != 0) {
            pixelsLeft -= 4;
            output.append(' ');
        }
        output.append(EnumChatFormat.COLOR_CHAR);
        output.append('l');
        while (pixelsLeft > 0) {
            pixelsLeft -= 5;
            output.append(' ');
        }
        output.append(EnumChatFormat.COLOR_CHAR);
        output.append('r');
        return output.toString();
    }

    public void updatePlayer(@NotNull TabPlayer target) {
        if (viewer.getVersion().getMinorVersion() < 8) return;
        playerWidths.put(target, getPlayerNameWidth(target));
        if (recalculateMaxWidth(null)) {
            updateAllPlayers();
        } else {
            viewer.getTabList().updateDisplayName(feature.getTablistUUID(target, viewer), formatName(target));
        }
    }

    public void processPlayerQuit(@NotNull TabPlayer disconnectedPlayer) {
        if (viewer.getVersion().getMinorVersion() < 8) return;
        if (disconnectedPlayer == maxPlayer && recalculateMaxWidth(disconnectedPlayer)) {
            updateAllPlayers();
        }
    }

    /**
     * Returns width of player's TabList name format
     *
     * @param   p
     *          player to get width for
     * @return  width of player's TabList name format
     */
    private int getPlayerNameWidth(@NotNull TabPlayer p) {
        return getTextWidth(IChatBaseComponent.fromColoredText(
                p.getProperty(TabConstants.Property.TABPREFIX).getFormat(viewer) +
                p.getProperty(TabConstants.Property.CUSTOMTABNAME).getFormat(viewer) +
                p.getProperty(TabConstants.Property.TABSUFFIX).getFormat(viewer)));
    }

    /**
     * Returns text width of characters in given component
     *
     * @param   component
     *          component to get width of
     * @return  text width of characters in given component
     */
    private int getTextWidth(@NotNull IChatBaseComponent component) {
        byte[] widths = feature.getWidths();
        Map<String, Integer> multiCharWidths = feature.getMultiCharWidths();
        int width = 0;
        if (component.getText() != null) {
            char[] chars = component.getText().toCharArray();
            for (int i=0; i<chars.length; i++) {
                if (component.getModifier().isBold()) {
                    width += 1;
                }
                if (i < chars.length-1) {
                    String key = (int)chars[i] + "+" + (int)chars[i + 1];
                    if (multiCharWidths.containsKey(key)) {
                        width += multiCharWidths.get(key) + 1;
                        i++; // Skip next character
                        continue;
                    }
                }
                width += widths[chars[i]] + 1;
            }
        }
        for (IChatBaseComponent extra : component.getExtra()) {
            width += getTextWidth(extra);
        }
        return width;
    }

    // returns true if max changed, false if not
    private boolean recalculateMaxWidth(@Nullable TabPlayer ignoredPlayer) {
        int newMaxWidth = 0;
        TabPlayer newMaxPlayer = null;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == ignoredPlayer || !playerWidths.containsKey(all)) continue;
            if (all.isVanished() && !canSeeVanished && all != viewer) continue;
            int localWidth = playerWidths.get(all);
            if (localWidth > newMaxWidth) {
                newMaxWidth = localWidth;
                newMaxPlayer = all;
            }
        }
        boolean changed = newMaxWidth != maxWidth;
        maxPlayer = newMaxPlayer;
        maxWidth = newMaxWidth;
        return changed;
    }

    public void onVanishChange(@NotNull TabPlayer changed) {
        if (viewer.getVersion().getMinorVersion() < 8) return;
        playerWidths.put(changed, getPlayerNameWidth(changed));
        if (recalculateMaxWidth(null)) {
            updateAllPlayers();
        } else if (!changed.isVanished() && !feature.getDisableChecker().isDisabledPlayer(changed)) {
            viewer.getTabList().updateDisplayName(changed.getTablistId(), feature.getTabFormat(changed, viewer));
        }
    }
}
