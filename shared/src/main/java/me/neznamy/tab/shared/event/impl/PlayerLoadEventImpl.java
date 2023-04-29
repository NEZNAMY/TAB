package me.neznamy.tab.shared.event.impl;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;

@Data
public final class PlayerLoadEventImpl implements PlayerLoadEvent {

    @NonNull private final TabPlayer player;
    private final boolean join;
}
