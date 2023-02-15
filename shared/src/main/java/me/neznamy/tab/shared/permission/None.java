package me.neznamy.tab.shared.permission;

import lombok.Getter;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabConstants;

/**
 * An instance of PermissionPlugin to be used when nothing is found
 */
public class None extends PermissionPlugin {

    @Getter private final String name = "Unknown/None";

    public None() {
        super(null);
    }

    @Override
    public String getPrimaryGroup(TabPlayer p) {
        return TabConstants.NO_GROUP;
    }
}