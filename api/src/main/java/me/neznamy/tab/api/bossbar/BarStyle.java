package me.neznamy.tab.api.bossbar;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An enum representing all available BossBar styles.
 * Calling ordinal() will return style's network ID.
 * Style names are equal to NMS names.
 */
@AllArgsConstructor
public enum BarStyle {

    PROGRESS("SOLID"),
    NOTCHED_6("SEGMENTED_6"),
    NOTCHED_10("SEGMENTED_10"),
    NOTCHED_12("SEGMENTED_12"),
    NOTCHED_20("SEGMENTED_20");

    /** Style's name used in Bukkit API */
    @Getter private final String bukkitName;
}