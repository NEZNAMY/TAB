package me.neznamy.tab.api.placeholder;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;

public interface RelationalPlaceholder extends Placeholder {

    void updateValue(@NonNull TabPlayer viewer, @NonNull TabPlayer target, @NonNull Object value);
}