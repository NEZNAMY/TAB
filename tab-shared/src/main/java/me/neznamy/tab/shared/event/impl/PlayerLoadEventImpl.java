package me.neznamy.tab.shared.event.impl;

import lombok.Data;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import org.jetbrains.annotations.NotNull;

@Data
public class PlayerLoadEventImpl implements PlayerLoadEvent {

    @NotNull private final TabPlayer player;
    private final boolean join;
}
