package me.neznamy.tab.shared.event.impl;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;

public final class PlayerLoadEventImpl implements PlayerLoadEvent {

    private final TabPlayer player;
    private final boolean join;

    public PlayerLoadEventImpl(TabPlayer player, boolean join) {
        this.player = player;
        this.join = join;
    }

    @Override
    public TabPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isJoin() {
        return join;
    }
}
