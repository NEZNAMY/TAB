package me.neznamy.tab.api.placeholder;

import lombok.NonNull;

public interface ServerPlaceholder extends Placeholder {

    void updateValue(@NonNull Object value);
}