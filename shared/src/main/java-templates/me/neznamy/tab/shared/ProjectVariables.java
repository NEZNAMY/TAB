package me.neznamy.tab.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A class containing project variables to expand by blossom plugin.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectVariables {

    public static final String PLUGIN_NAME = "{{ name }}";
    public static final String PLUGIN_ID = "{{ id }}";
    public static final String PLUGIN_VERSION = "{{ version }}";
    public static final String PLUGIN_DESCRIPTION = "{{ description }}";
    public static final String PLUGIN_WEBSITE = "{{ website }}";
    public static final String PLUGIN_AUTHOR = "{{ author }}";
}
