package me.neznamy.tab.api.nametag;

import java.util.List;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UnlimitedNameTagManager extends NameTagManager {

    void disableArmorStands(@NonNull TabPlayer player);
    
    void enableArmorStands(@NonNull TabPlayer player);
    
    boolean hasDisabledArmorStands(@NonNull TabPlayer player);

    void setName(@NonNull TabPlayer player, @Nullable String customName);
    
    void setLine(@NonNull TabPlayer player, @NonNull String line, @Nullable String value);

    @Nullable String getCustomName(@NonNull TabPlayer player);
    
    @Nullable String getCustomLineValue(@NonNull TabPlayer player, @NonNull String line);

    @NotNull String getOriginalName(@NonNull TabPlayer player);
    
    @NotNull String getOriginalLineValue(@NonNull TabPlayer player, @NonNull String line);
    
    @NotNull List<String> getDefinedLines();
}
