package me.neznamy.tab.shared.proxy;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract class for player on proxy containing variables and methods
 * shared between proxies.
 */
@Getter @Setter
public abstract class ProxyTabPlayer extends TabPlayer {

    /** Player's vanish status from backend server */
    private boolean vanished;

    /** Player's disguise status from backend server */
    private boolean disguised;

    /** Player's invisibility potion status from backend server */
    private boolean invisibilityPotion;

    /** Player's boat vehicle status for unlimited NameTags */
    private boolean onBoat;

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
    protected ProxyTabPlayer(@NotNull Object player, @NotNull UUID uniqueId, @NotNull String name, @NotNull String server, int protocolVersion) {
        super(player, uniqueId, name, server, "N/A", protocolVersion, TAB.getInstance().getConfiguration().isOnlineUuidInTabList());
        sendJoinPluginMessage();
    }

    /**
     * Sends plugin message to backend server that this player has
     * joined, containing all plugin configuration data.
     */
    public void sendJoinPluginMessage() {
        bridgeConnected = false; // Reset on server switch
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        List<Object> args = Lists.newArrayList(
                "PlayerJoin",
                getVersion().getNetworkId(),
                TAB.getInstance().getGroupManager().getPermissionPlugin().contains("Vault") &&
                        !TAB.getInstance().getGroupManager().isGroupsByPermissions(),
                !(expansion instanceof EmptyTabExpansion));
        ProxyPlatform platform = (ProxyPlatform) TAB.getInstance().getPlatform();
        Map<String, Integer> placeholders = platform.getBridgePlaceholders();
        args.add(placeholders.size());
        for (Map.Entry<String, Integer> entry : placeholders.entrySet()) {
            args.add(entry.getKey());
            args.add(entry.getValue());
        }
        NameTagX nametagx = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS);
        boolean enabled = nametagx != null;
        args.add(enabled);
        if (enabled) {
            args.add(nametagx.isDisableOnBoats());
            args.add(nametagx.isArmorStandsAlwaysVisible());
            args.add(nametagx.getDisableChecker().isDisabledPlayer(this) || nametagx.getUnlimitedDisableChecker().isDisabledPlayer(this));
            args.add(nametagx.getDynamicLines().size());
            args.addAll(nametagx.getDynamicLines());
            args.add(nametagx.getStaticLines().size());
            for (Map.Entry<String, Object> entry : nametagx.getStaticLines().entrySet()) {
                args.add(entry.getKey());
                args.add(Double.valueOf(String.valueOf(entry.getValue())));
            }
        }
        Map<String, Map<Object, Object>> replacements = TAB.getInstance().getConfig().getConfigurationSection("placeholder-output-replacements");
        args.add(replacements.size());
        for (Map.Entry<String, Map<Object, Object>> entry : replacements.entrySet()) {
            args.add(entry.getKey());
            args.add(entry.getValue().size());
            for (Map.Entry<Object, Object> rule : entry.getValue().entrySet()) {
                args.add(EnumChatFormat.color(String.valueOf(rule.getKey())));
                args.add(EnumChatFormat.color(String.valueOf(rule.getValue())));
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
    public void sendPluginMessage(@NotNull Object... args) {
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
    private void writeObject(@NotNull ByteArrayDataOutput out, @NotNull Object value) {
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

    /**
     * Prints an error message saying player was not connected to any server,
     * therefore plugin message could not be sent.
     *
     * @param   message
     *          Message that failed to send
     */
    public void errorNoServer(byte[] message) {
        TAB.getInstance().getErrorManager().printError("Skipped plugin message send to " + getName() + ", because player is not" +
                "connected to any server (message=" + new String(message) + ")");
    }
}