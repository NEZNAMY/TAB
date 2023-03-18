package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.shared.features.PlayerList;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerSlot {

    private final Layout layout;
    @Getter private final UUID uniqueId;
    @Getter private TabPlayer player;
    private String text = "";

    public void setPlayer(TabPlayer newPlayer) {
        if (player == newPlayer) return;
        this.player = newPlayer;
        if (player != null) text = "";
        for (TabPlayer viewer : layout.getViewers()) {
            if (viewer.getVersion().getMinorVersion() < 8 || viewer.isBedrockPlayer()) continue;
            viewer.getTabList().removeEntry(uniqueId);
            viewer.getTabList().addEntry(getSlot(viewer));
        }
    }

    public TabListEntry getSlot(TabPlayer p) {
        TabListEntry data;
        TabPlayer player = this.player; //avoiding NPE from concurrent access
        if (player != null) {
            PlayerList playerList = layout.getManager().getPlayerList();
            data = new TabListEntry(
                    uniqueId,
                    layout.getEntryName(p, uniqueId.getLeastSignificantBits()),
                    player.getSkin(),
                    true,
                    player.getPing(),
                    0,
                    playerList == null ? new IChatBaseComponent(player.getName()) : playerList.getTabFormat(player, p),
                    null
            );
        } else {
            data = new TabListEntry(
                    uniqueId,
                    layout.getEntryName(p, uniqueId.getLeastSignificantBits()),
                    layout.getManager().getSkinManager().getDefaultSkin(),
                    true,
                    layout.getManager().getEmptySlotPing(),
                    0,
                    new IChatBaseComponent(text),
                    null
            );
        }
        return data;
    }

    public void setText(String text) {
        if (this.text.equals(text) && player == null) return;
        this.text = text;
        if (player != null) {
            setPlayer(null);
        } else {
            for (TabPlayer all : layout.getViewers()) {
                if (all.getVersion().getMinorVersion() < 8 || all.isBedrockPlayer()) continue;
                all.getTabList().updateDisplayName(uniqueId, IChatBaseComponent.optimizedComponent(text));
            }
        }
    }
}
