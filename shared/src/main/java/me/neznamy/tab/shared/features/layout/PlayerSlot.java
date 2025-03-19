package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.tab.shared.features.playerlist.PlayerList;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerSlot {

    private static final StringToComponentCache cache = new StringToComponentCache("LayoutPlayerSlot", 100);

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

    public @NotNull TabList.Entry getSlot(@NotNull TabPlayer viewer) {
        TabList.Entry data;
        TabPlayer player = this.player; //avoiding NPE from concurrent access
        if (player != null) {
            PlayerList playerList = layout.getManager().getPlayerList();
            data = new TabList.Entry(
                    uniqueId,
                    layout.getManager().getConfiguration().getDirection().getEntryName(viewer, slot, LayoutManagerImpl.isTeamsEnabled()),
                    player.getSkin(),
                    true,
                    layout.getManager().getPingSpoof() != null ? layout.getManager().getPingSpoof().getConfiguration().getValue() : player.getPing(),
                    0,
                    playerList == null || player.tablistData.disabled.get() ? SimpleTextComponent.text(player.getName()) : playerList.getTabFormat(player, viewer),
                    Integer.MAX_VALUE - layout.getManager().getConfiguration().getDirection().translateSlot(slot),
                    true
            );
        } else {
            data = new TabList.Entry(
                    uniqueId,
                    layout.getManager().getConfiguration().getDirection().getEntryName(viewer, slot, LayoutManagerImpl.isTeamsEnabled()),
                    layout.getManager().getSkinManager().getDefaultSkin(slot),
                    true,
                    layout.getManager().getConfiguration().getEmptySlotPing(),
                    0,
                    SimpleTextComponent.text(text),
                    Integer.MAX_VALUE - layout.getManager().getConfiguration().getDirection().translateSlot(slot),
                    true
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
            layout.getViewer().getTabList().updateDisplayName(uniqueId, cache.get(text));
        }
    }
}
