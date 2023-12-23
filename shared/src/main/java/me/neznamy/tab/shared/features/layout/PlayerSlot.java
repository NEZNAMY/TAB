package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.PlayerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerSlot {

    private final int slot;
    private final LayoutView layout;
    @Getter private final UUID uniqueId;
    @Getter private TabPlayer player;
    private String text = "";

    public void setPlayer(@Nullable TabPlayer newPlayer) {
        if (player == newPlayer) return;
        player = newPlayer;
        if (player != null) text = "";
        if (layout.getViewer().getVersion().getMinorVersion() < 8 || layout.getViewer().isBedrockPlayer()) return;
        layout.getViewer().getTabList().removeEntry(uniqueId);
        layout.getViewer().getTabList().addEntry(getSlot(layout.getViewer()));
    }

    public @NotNull TabList.Entry getSlot(@NotNull TabPlayer p) {
        TabList.Entry data;
        TabPlayer player = this.player; //avoiding NPE from concurrent access
        if (player != null) {
            PlayerList playerList = layout.getManager().getPlayerList();
            data = new TabList.Entry(
                    uniqueId,
                    layout.getManager().getDirection().getEntryName(p, slot),
                    player.getSkin(),
                    player.getPing(),
                    0,
                    playerList == null ? new IChatBaseComponent(player.getName()) : playerList.getTabFormat(player, p)
            );
        } else {
            data = new TabList.Entry(
                    uniqueId,
                    layout.getManager().getDirection().getEntryName(p, slot),
                    layout.getManager().getSkinManager().getDefaultSkin(slot),
                    layout.getManager().getEmptySlotPing(),
                    0,
                    new IChatBaseComponent(text)
            );
        }
        return data;
    }

    public void setText(@NotNull String text) {
        if (this.text.equals(text) && player == null) return;
        this.text = text;
        if (player != null) {
            setPlayer(null);
        } else {
            if (layout.getViewer().getVersion().getMinorVersion() < 8 || layout.getViewer().isBedrockPlayer()) return;
            layout.getViewer().getTabList().updateDisplayName(uniqueId, IChatBaseComponent.optimizedComponent(text));
        }
    }
}
