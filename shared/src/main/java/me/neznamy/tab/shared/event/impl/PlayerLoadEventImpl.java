package me.neznamy.tab.shared.event.impl;

import lombok.Data;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;

@Data
public final class PlayerLoadEventImpl implements PlayerLoadEvent {

    private final TabPlayer player;
    private final boolean join;
}
