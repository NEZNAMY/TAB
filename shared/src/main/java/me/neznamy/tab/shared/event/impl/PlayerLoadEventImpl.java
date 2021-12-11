package me.neznamy.tab.shared.event.impl;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;

public final class PlayerLoadEventImpl implements PlayerLoadEvent {

    private final TabPlayer player;

    public PlayerLoadEventImpl(final TabPlayer player) {
        this.player = player;
    }

    @Override
    public TabPlayer getPlayer() {
        return player;
    }
}
