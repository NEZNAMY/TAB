package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.api.*;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.layout.Layout;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.layout.PlayerSlot;
import me.neznamy.tab.shared.features.redis.RedisSupport;

/**
 * Feature handler for TabList display names
 */
public class PlayerList extends TabFeature implements TablistFormatManager {

    /** Config option toggling anti-override which prevents other plugins from overriding TAB */
    protected final boolean antiOverrideTabList = TAB.getInstance().getConfiguration().getConfig().getBoolean("tablist-name-formatting.anti-override", true);

    /**
     * Flag tracking when the plugin is disabling to properly clear
     * display name by setting it to null value and not force the value back
     * with the anti-override.
     */
    private boolean disabling = false;

    /**
     * Constructs new instance and sends debug message that feature loaded.
     */
    public PlayerList() {
        super("TabList prefix/suffix", "Updating TabList format", "tablist-name-formatting");
        TAB.getInstance().debug(String.format("Loaded PlayerList feature with parameters disabledWorlds=%s, disabledServers=%s, antiOverrideTabList=%s", Arrays.toString(disabledWorlds), Arrays.toString(disabledServers), antiOverrideTabList));
    }

    /**
     * Returns UUID of tablist entry representing this player. If layout feature
     * is enabled, returns UUID of the layout slot where the player should be.
     * When it's not enabled, returns player's TabList UUID, which may not match
     * with player's actual UUID due to velocity.
     *
     * @param   p
     *          Player to get tablist UUID of
     * @param   viewer
     *          TabList viewer
     * @return  UUID of TabList entry representing requested player
     */
    public UUID getTablistUUID(TabPlayer p, TabPlayer viewer) {
        LayoutManager manager = (LayoutManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
        if (manager != null) {
            Layout layout = manager.getPlayerViews().get(viewer);
            if (layout != null) {
                PlayerSlot slot = layout.getSlot(p);
                if (slot != null) {
                    return slot.getUUID();
                }
            }
        }
        return p.getTablistUUID(); //layout not enabled or player not visible to viewer
    }

    /**
     * Loads all properties from config and returns {@code true} if at least
     * one of them either wasn't loaded or changed value, {@code false} otherwise.
     *
     * @param   p
     *          Player to update properties of
     * @return  {@code true} if at least one property changed, {@code false} if not
     */
    protected boolean updateProperties(TabPlayer p) {
        boolean changed = p.loadPropertyFromConfig(this, TabConstants.Property.TABPREFIX);
        if (p.loadPropertyFromConfig(this, TabConstants.Property.CUSTOMTABNAME, p.getName())) changed = true;
        if (p.loadPropertyFromConfig(this, TabConstants.Property.TABSUFFIX)) changed = true;
        return changed;
    }

    /**
     * Updates TabList format of requested player to everyone.
     *
     * @param   p
     *          Player to update
     * @param   format
     *          Whether player's actual format should be used or {@code null} for reset
     */
    protected void updatePlayer(TabPlayer p, boolean format) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
                    new PlayerInfoData(getTablistUUID(p, viewer), format ? getTabFormat(p, viewer) : null)), this);
        }
        RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        if (redis != null) redis.updateTabFormat(p, p.getProperty(TabConstants.Property.TABPREFIX).get() + p.getProperty(TabConstants.Property.CUSTOMTABNAME).get() + p.getProperty(TabConstants.Property.TABSUFFIX).get());
    }

    /**
     * Returns TabList format of player for viewer
     *
     * @param   p
     *          Player to get format of
     * @param   viewer
     *          Viewer seeing the format
     * @return  Format of specified player for viewer
     */
    public IChatBaseComponent getTabFormat(TabPlayer p, TabPlayer viewer) {
        Property prefix = p.getProperty(TabConstants.Property.TABPREFIX);
        Property name = p.getProperty(TabConstants.Property.CUSTOMTABNAME);
        Property suffix = p.getProperty(TabConstants.Property.TABSUFFIX);
        if (prefix == null || name == null || suffix == null) {
            return null;
        }
        return IChatBaseComponent.optimizedComponent(prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer));
    }

    @Override
    public void load(){
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (isDisabled(all.getServer(), all.getWorld())) {
                addDisabledPlayer(all);
                updateProperties(all);
                continue;
            }
            refresh(all, true);
        }
    }

    @Override
    public void unload(){
        disabling = true;
        List<PlayerInfoData> updatedPlayers = new ArrayList<>();
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (!isDisabledPlayer(p)) updatedPlayers.add(new PlayerInfoData(getTablistUUID(p, p)));
        }
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.getVersion().getMinorVersion() >= 8) all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers), this);
        }
    }

    @Override
    public void onServerChange(TabPlayer p, String from, String to) {
        onWorldChange(p, null, null);
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) return;
        TAB.getInstance().getCPUManager().runTaskLater(300, this, TabConstants.CpuUsageCategory.PLAYER_JOIN, () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (p.getVersion().getMinorVersion() >= 8) p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
                        new PlayerInfoData(getTablistUUID(all, p), getTabFormat(all, p))), this);
                if (all.getVersion().getMinorVersion() >= 8) all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
                        new PlayerInfoData(getTablistUUID(p, all), getTabFormat(p, all))), this);
            }
        });
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        boolean disabledBefore = isDisabledPlayer(p);
        boolean disabledNow = false;
        if (isDisabled(p.getServer(), p.getWorld())) {
            disabledNow = true;
            addDisabledPlayer(p);
        } else {
            removeDisabledPlayer(p);
        }
        if (disabledNow) {
            if (!disabledBefore) {
                updatePlayer(p, false);
            }
        } else if (updateProperties(p)) {
            updatePlayer(p, true);
        }
    }

    @Override
    public void refresh(TabPlayer refreshed, boolean force) {
        if (isDisabledPlayer(refreshed)) return;
        boolean refresh;
        if (force) {
            updateProperties(refreshed);
            refresh = true;
        } else {
            boolean prefix = refreshed.getProperty(TabConstants.Property.TABPREFIX).update();
            boolean name = refreshed.getProperty(TabConstants.Property.CUSTOMTABNAME).update();
            boolean suffix = refreshed.getProperty(TabConstants.Property.TABSUFFIX).update();
            refresh = prefix || name || suffix;
        }
        if (refresh) {
            updatePlayer(refreshed, true);
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        updateProperties(connectedPlayer);
        if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
            addDisabledPlayer(connectedPlayer);
            return;
        }
        Runnable r = () -> {
            refresh(connectedPlayer, true);
            if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
            List<PlayerInfoData> list = new ArrayList<>();
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == connectedPlayer) continue; //already sent 4 lines above
                list.add(new PlayerInfoData(getTablistUUID(all, connectedPlayer), getTabFormat(all, connectedPlayer)));
            }
            if (!list.isEmpty()) connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list), this);
        };
        r.run();
        //add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
        if (!antiOverrideTabList || !TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION) ||
                connectedPlayer.getVersion().getMinorVersion() == 8)
            TAB.getInstance().getCPUManager().runTaskLater(300, this, TabConstants.CpuUsageCategory.PLAYER_JOIN, r);
    }

    @Override
    public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
        if (disabling || !antiOverrideTabList) return;
        if (info.getAction() != EnumPlayerInfoAction.UPDATE_DISPLAY_NAME && info.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
        for (PlayerInfoData playerInfoData : info.getEntries()) {
            TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(playerInfoData.getUniqueId());
            if (packetPlayer != null && !isDisabledPlayer(packetPlayer) && packetPlayer.getTablistUUID() == getTablistUUID(packetPlayer, receiver)) {
                playerInfoData.setDisplayName(getTabFormat(packetPlayer, receiver));
            }
        }
    }

    @Override
    public void setPrefix(TabPlayer player, String prefix) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TABPREFIX).setTemporaryValue(prefix);
        updatePlayer(player, true);
    }

    @Override
    public void setName(TabPlayer player, String customName) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.CUSTOMTABNAME).setTemporaryValue(customName);
        updatePlayer(player, true);
    }

    @Override
    public void setSuffix(TabPlayer player, String suffix) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TABSUFFIX).setTemporaryValue(suffix);
        updatePlayer(player, true);
    }

    @Override
    public void resetPrefix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TABPREFIX).setTemporaryValue(null);
        updatePlayer(player, true);
    }

    @Override
    public void resetName(TabPlayer player) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.CUSTOMTABNAME).setTemporaryValue(null);
        updatePlayer(player, true);
    }

    @Override
    public void resetSuffix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TABSUFFIX).setTemporaryValue(null);
        updatePlayer(player, true);
    }

    @Override
    public String getCustomPrefix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TABPREFIX).getTemporaryValue();
    }

    @Override
    public String getCustomName(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.CUSTOMTABNAME).getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TABSUFFIX).getTemporaryValue();
    }

    @Override
    public String getOriginalPrefix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TABPREFIX).getOriginalRawValue();
    }

    @Override
    public String getOriginalName(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.CUSTOMTABNAME).getOriginalRawValue();
    }

    @Override
    public String getOriginalSuffix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TABSUFFIX).getOriginalRawValue();
    }
}