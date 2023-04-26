package me.neznamy.tab.api.placeholder;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;

public interface PlayerPlaceholder extends Placeholder {

    void updateValue(@NonNull TabPlayer player, @NonNull Object value);
}