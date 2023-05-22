package me.neznamy.tab.api.tablist.layout;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Layout {

    @NotNull String getName();

    void addFixedSlot(int slot, @NonNull String text);

    void addFixedSlot(int slot, @NonNull String text, @NonNull String skin);

    void addFixedSlot(int slot, @NonNull String text, int ping);

    void addFixedSlot(int slot, @NonNull String text, @NonNull String skin, int ping);

    void addGroup(@Nullable String condition, int[] slots);
}
