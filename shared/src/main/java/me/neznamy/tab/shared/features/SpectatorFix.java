package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;

/**
 * Cancelling GameMode change packet to spectator GameMode to avoid players being moved on
 * the bottom of TabList with transparent name. Does not work on self as that would result
 * in players not being able to clip through walls.
 */
public class SpectatorFix extends TabFeature {

    /**
     * Constructs new instance
     */
    public SpectatorFix() {
        super("Spectator fix", null);
    }

    /**
     * Sends GameMode update of all players to either their real GameMode if
     * {@code realGameMode} is {@code true} or fake value if it's {@code false}.
     *
     * @param   viewer
     *          Player to send gamemode updates to
     * @param   realGameMode
     *          Whether real GameMode should be shown or fake one
     */
    private void updatePlayer(TabPlayer viewer, boolean realGameMode) {
        if (viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) return;
        List<PlayerInfoData> list = new ArrayList<>();
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            if (viewer == target || target.getGamemode() != 3) continue;
            list.add(new PlayerInfoData(target.getUniqueId(), realGameMode ? EnumGamemode.VALUES[target.getGamemode()+1] : EnumGamemode.CREATIVE));
        }
        if (!list.isEmpty()) viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_GAME_MODE, list), this);
    }

    @Override
    public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
        if (!info.getActions().contains(EnumPlayerInfoAction.UPDATE_GAME_MODE)) return;
        for (PlayerInfoData playerInfoData : info.getEntries()) {
            if (playerInfoData.getGameMode() != EnumGamemode.SPECTATOR) continue;
            if (receiver.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) continue;
            TabPlayer changed = TAB.getInstance().getPlayerByTabListUUID(playerInfoData.getUniqueId());
            if (changed != receiver) playerInfoData.setGameMode(EnumGamemode.CREATIVE);
        }
    }

    @Override
    public void onJoin(TabPlayer p) {
        updatePlayer(p, false);
    }

    @Override
    public void load() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            updatePlayer(viewer, false);
        }
    }

    @Override
    public void unload() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            updatePlayer(viewer, true);
        }
    }
}