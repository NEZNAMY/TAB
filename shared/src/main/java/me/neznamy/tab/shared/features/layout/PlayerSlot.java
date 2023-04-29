package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
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

    private final Layout layout;
    @Getter private final UUID uniqueId;
    @Getter private TabPlayer player;
    private String text = "";

    public void setPlayer(@Nullable TabPlayer newPlayer) {
        if (player == newPlayer) return;
        this.player = newPlayer;
        if (player != null) text = "";
        for (TabPlayer viewer : layout.getViewers()) {
            if (viewer.getVersion().getMinorVersion() < 8 || viewer.isBedrockPlayer()) continue;
            viewer.getTabList().removeEntry(uniqueId);
            viewer.getTabList().addEntry(getSlot(viewer));
        }
    }

    public @NotNull TabList.Entry getSlot(@NonNull TabPlayer p) {
        TabList.Entry data;
        TabPlayer player = this.player; //avoiding NPE from concurrent access
        if (player != null) {
            PlayerList playerList = layout.getManager().getPlayerList();
            data = new TabList.Entry(
                    uniqueId,
                    layout.getEntryName(p, uniqueId.getLeastSignificantBits()),
                    player.getSkin(),
                    player.getPing(),
                    0,
                    playerList == null ? new IChatBaseComponent(player.getName()) : playerList.getTabFormat(player, p)
            );
        } else {
            data = new TabList.Entry(
                    uniqueId,
                    layout.getEntryName(p, uniqueId.getLeastSignificantBits()),
                    layout.getManager().getSkinManager().getDefaultSkin(),
                    layout.getManager().getEmptySlotPing(),
                    0,
                    new IChatBaseComponent(text)
            );
        }
        return data;
    }

    public void setText(@NonNull String text) {
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
