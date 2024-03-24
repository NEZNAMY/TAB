package me.neznamy.tab.shared.proxy;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.proxy.message.outgoing.OutgoingMessage;
import me.neznamy.tab.shared.proxy.message.outgoing.PermissionRequest;
import me.neznamy.tab.shared.proxy.message.outgoing.PlayerJoin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract class for player on proxy containing variables and methods
 * shared between proxies.
 */
@Getter @Setter
public abstract class ProxyTabPlayer extends TabPlayer {

    /** Player's vanish status from backend server */
    public boolean vanished;

    /** Player's disguise status from backend server */
    private boolean disguised;

    /** Player's invisibility potion status from backend server */
    private boolean invisibilityPotion;

    /** Player's boat vehicle status for unlimited NameTags */
    private boolean onBoat;

    /** Timestamp when join plugin message was sent to track how long bridge took to respond */
    private long bridgeRequestTime;

    /** Flag for marking if this player has received join response from bridge or not */
    private boolean bridgeConnected;

    /** Player's gamemode */
    private int gamemode;

    /** Map of player's requested permissions */
    private final Map<String, Boolean> permissions = new HashMap<>();

    /**
     * Constructs new instance with given parameters and sends a message
     * to bridge about this player joining with join data
     *
     * @param   platform
     *          Server platform
     * @param   player
     *          platform-specific player object
     * @param   uniqueId
     *          Player's unique ID
     * @param   name
     *          Player's name
     * @param   server
     *          Player's server
     * @param   protocolVersion
     *          Player's protocol network id
     */
    protected ProxyTabPlayer(@NotNull ProxyPlatform platform, @NotNull Object player, @NotNull UUID uniqueId,
                             @NotNull String name, @NotNull String server, int protocolVersion) {
        super(platform, player, uniqueId, name, server, "N/A", protocolVersion, TAB.getInstance().getConfiguration().isOnlineUuidInTabList());
        sendJoinPluginMessage();
    }

    /**
     * Sends plugin message to backend server that this player has
     * joined, containing all plugin configuration data.
     */
    public void sendJoinPluginMessage() {
        bridgeConnected = false; // Reset on server switch

        PlayerJoin.UnlimitedNametagSettings settings = null;
        NameTagX nametagx = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS);
        if (nametagx != null) {
            settings = new PlayerJoin.UnlimitedNametagSettings(
                    nametagx.isDisableOnBoats(),
                    nametagx.isArmorStandsAlwaysVisible(),
                    nametagx.getDisableChecker().isDisabledPlayer(this) || nametagx.getUnlimitedDisableChecker().isDisabledPlayer(this),
                    nametagx.getDynamicLines(),
                    nametagx.getStaticLines()
            );
        }
        sendPluginMessage(new PlayerJoin(
                getVersion().getNetworkId(),
                TAB.getInstance().getGroupManager().getPermissionPlugin().contains("Vault") &&
                    !TAB.getInstance().getGroupManager().isGroupsByPermissions(),
                ((ProxyPlatform) getPlatform()).getBridgePlaceholders(),
                TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholder-output-replacements"),
                settings
        ));
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        if (expansion instanceof ProxyTabExpansion) {
            ((ProxyTabExpansion) expansion).resendAllValues(this);
        }
        bridgeRequestTime = System.currentTimeMillis();
    }

    /**
     * Sets permission presence status to provided value
     *
     * @param   permission
     *          Requested permission node
     * @param   value
     *          Permission value
     */
    public void setHasPermission(@NotNull String permission, boolean value) {
        permissions.put(permission, value);
    }

    /**
     * Performs platform-specific permission check and returns the result
     *
     * @param   permission
     *          Permission to check for
     * @return  Result from hasPermission call
     */
    public abstract boolean hasPermission0(String permission);

    /**
     * Sends plugin message to this player
     *
     * @param   message
     *          message to send
     */
    public abstract void sendPluginMessage(byte[] message);

    @Override
    public boolean hasInvisibilityPotion() {
        return invisibilityPotion;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
            sendPluginMessage(new PermissionRequest(permission));
            return permissions != null && permissions.getOrDefault(permission, false);
        }
        return hasPermission0(permission);
    }

    /**
     * Sends plugin message to the player.
     *
     * @param   message
     *          Plugin message to send
     */
    public void sendPluginMessage(@NotNull OutgoingMessage message) {
        sendPluginMessage(message.write().toByteArray());
    }
}