package me.neznamy.tab.api.event.plugin;

import lombok.NonNull;
import me.neznamy.tab.api.event.TabEvent;
import me.neznamy.tab.api.placeholder.Placeholder;
import org.jetbrains.annotations.NotNull;

public interface PlaceholderRegisterEvent extends TabEvent {

    @NotNull String getIdentifier();

    void setPlaceholder(@NonNull Placeholder placeholder);
}
