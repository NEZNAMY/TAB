package me.neznamy.tab.shared.proxy;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.permission.VaultBridge;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract class for player on proxy containing variables and methods
 * shared between proxies.
 */
public abstract class ProxyTabPlayer extends ITabPlayer {

    /** Player's vanish status from backend server */
    @Getter @Setter private boolean vanished;

    /** Player's disguise status from backend server */
    @Getter @Setter private boolean disguised;

    /** Player's invisibility potion status from backend server */
    @Setter private boolean invisibilityPotion;

    /** Player's boat vehicle status for unlimited NameTags */
    @Getter @Setter private boolean onBoat;

    /** Map of player's requested permissions */
    private final Map<String, Boolean> permissions = new HashMap<>();

    /**
     * Constructs new instance with given parameters and sends a message
     * to bridge about this player joining with join data
     *
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
    protected ProxyTabPlayer(Object player, UUID uniqueId, String name, String server, int protocolVersion) {
        super(player, uniqueId, name, server, "N/A", protocolVersion, TAB.getInstance().getConfiguration().isOnlineUuidInTabList());
        sendJoinPluginMessage();
    }

    /**
     * Sends plugin message to backend server that this player has
     * joined, containing all plugin configuration data.
     */
    public void sendJoinPluginMessage() {
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        List<Object> args = Lists.newArrayList("PlayerJoin", getVersion().getNetworkId(),
                TAB.getInstance().getGroupManager().getPlugin() instanceof VaultBridge && !TAB.getInstance().getGroupManager().isGroupsByPermissions(),
                TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PET_FIX),
                !(expansion instanceof EmptyTabExpansion));
        ProxyPlatform platform = (ProxyPlatform) TAB.getInstance().getPlatform();
        Map<String, Integer> placeholders = platform.getBridgePlaceholders();
        args.add(placeholders.size());
        for (Map.Entry<String, Integer> entry : placeholders.entrySet()) {
            args.add(entry.getKey());
            args.add(entry.getValue());
        }
        NameTagX nametagx = (NameTagX) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS);
        boolean enabled = nametagx != null && !nametagx.getDisabledUnlimitedServers().contains(getServer());
        args.add(enabled);
        if (enabled) {
            args.add(true);
            args.add(0.26d);
            args.add(nametagx.isDisableOnBoats());
            args.add(nametagx.isArmorStandsAlwaysVisible());
            args.add(nametagx.getDisabledUnlimitedWorlds().size());
            args.addAll(nametagx.getDisabledUnlimitedWorlds());
            args.add(nametagx.getDynamicLines().size());
            args.addAll(nametagx.getDynamicLines());
            args.add(nametagx.getStaticLines().size());
            for (Map.Entry<String, Object> entry : nametagx.getStaticLines().entrySet()) {
                args.add(entry.getKey());
                args.add(Double.valueOf(String.valueOf(entry.getValue())));
            }
        }
        sendPluginMessage(args.toArray());
        if (expansion instanceof ProxyTabExpansion) ((ProxyTabExpansion) expansion).resendAllValues(this);
    }

    /**
     * Sets permission presence status to provided value
     *
     * @param   permission
     *          Requested permission node
     * @param   value
     *          Permission value
     */
    public void setHasPermission(String permission, boolean value) {
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

    /**
     * Returns player's chat signing key (1.19+), chat session (1.19.3+)
     *
     * @return  Player's chat session
     */
    public abstract Object getChatSession();

    @Override
    public boolean hasInvisibilityPotion() {
        return invisibilityPotion;
    }

    @Override
    public boolean hasPermission(String permission) {
        if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
            sendPluginMessage("Permission", permission);
            return permissions != null && permissions.getOrDefault(permission, false);
        }
        return hasPermission0(permission);
    }

    /**
     * Sends plugin message
     *
     * @param   args
     *          Messages to encode
     */
    @SuppressWarnings("UnstableApiUsage")
    public void sendPluginMessage(Object... args) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        for (Object arg : args) {
            writeObject(out, arg);
        }
        sendPluginMessage(out.toByteArray());
    }

    /**
     * Writes object to data input by calling proper write method
     * based on data type of the object.
     *
     * @param   out
     *          Data output to write to
     * @param   value
     *          Value to write
     */
    private void writeObject(@NonNull ByteArrayDataOutput out, @NonNull Object value) {
        if (value instanceof String) {
            out.writeUTF((String) value);
        } else if (value instanceof Boolean) {
            out.writeBoolean((boolean) value);
        } else if (value instanceof Integer) {
            out.writeInt((int) value);
        } else if (value instanceof Double) {
            out.writeDouble((double) value);
        } else throw new IllegalArgumentException("Unhandled message data type " + value.getClass().getName());
    }
}