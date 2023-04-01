package me.neznamy.tab.api.team;

import java.util.List;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

public interface UnlimitedNametagManager extends TeamManager {

    void disableArmorStands(TabPlayer player);
    
    void enableArmorStands(TabPlayer player);
    
    boolean hasDisabledArmorStands(TabPlayer player);

    void setName(@NonNull TabPlayer player, @Nullable String customName);
    
    void setLine(@NonNull TabPlayer player, @NonNull String line, @Nullable String value);

    String getCustomName(TabPlayer player);
    
    String getCustomLineValue(TabPlayer player, String line);

    String getOriginalName(TabPlayer player);
    
    String getOriginalLineValue(TabPlayer player, String line);
    
    List<String> getDefinedLines();
}
