package me.neznamy.tab.api.placeholder;

import org.jetbrains.annotations.NotNull;

public interface Placeholder {

    int getRefresh();

    @NotNull String getIdentifier();
}