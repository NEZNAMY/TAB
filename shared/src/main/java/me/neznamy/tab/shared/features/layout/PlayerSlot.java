package me.neznamy.tab.shared.features.layout;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlayerList;

public class PlayerSlot {

    private final PlayerList playerlist = (PlayerList) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);
    private final Layout layout;
    private final UUID id;
    private TabPlayer player;
    private String text = "";

    public PlayerSlot(Layout layout, UUID id) {
        this.layout = layout;
        this.id = id;
    }

    public UUID getUUID() {
        return id;
    }

    public void setPlayer(TabPlayer newPlayer) {
        if (player == newPlayer) return;
        this.player = newPlayer;
        if (player != null) text = "";
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(id));
        for (TabPlayer viewer : layout.getViewers()) {
            if (viewer.getVersion().getMinorVersion() < 8 || viewer.isBedrockPlayer()) continue;
            viewer.sendCustomPacket(packet, TabConstants.PacketCategory.LAYOUT_PLAYER_SLOTS);
            viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, getSlot(viewer)), TabConstants.PacketCategory.LAYOUT_PLAYER_SLOTS);
        }
    }

    public PlayerInfoData getSlot(TabPlayer p) {
        PlayerInfoData data;
        TabPlayer player = this.player; //avoiding NPE from concurrent access
        if (player != null) {
            data = new PlayerInfoData("", id, player.getSkin(), player.getPing(), EnumGamemode.SURVIVAL, playerlist == null ? new IChatBaseComponent(player.getName()) : playerlist.getTabFormat(player, p));
        } else {
            data = new PlayerInfoData("", id, layout.getManager().getSkinManager().getDefaultSkin(), layout.getManager().getEmptySlotPing(), EnumGamemode.SURVIVAL, new IChatBaseComponent(text));
        }
        return data;
    }

    public void setText(String text) {
        if (this.text.equals(text) && player == null) return;
        this.text = text;
        if (player != null) {
            setPlayer(null);
        } else {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(id, IChatBaseComponent.optimizedComponent(text)));
            for (TabPlayer all : layout.getViewers()) {
                if (all.getVersion().getMinorVersion() < 8 || all.isBedrockPlayer()) continue;
                all.sendCustomPacket(packet, TabConstants.PacketCategory.LAYOUT_PLAYER_SLOTS);
            }
        }
    }
}
