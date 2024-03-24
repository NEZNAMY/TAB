package me.neznamy.tab.api.bossbar;

/**
 * An enum representing all available BossBar styles.
 * Calling ordinal() will return style's network ID.
 */
@SuppressWarnings("unused") // API class
public enum BarStyle {

    /** Fully solid line */
    PROGRESS,

    /** Line segmented into 6 parts */
    NOTCHED_6,

    /** Line segmented into 10 parts */
    NOTCHED_10,

    /** Line segmented into 12 parts */
    NOTCHED_12,

    /** Line segmented into 20 parts */
    NOTCHED_20
}