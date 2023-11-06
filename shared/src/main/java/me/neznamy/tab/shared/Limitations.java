package me.neznamy.tab.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class containing constants representing value length
 * limits on different versions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Limitations {

    /** Player name length limit for scoreboard score on 1.7 and lower */
    public static final int SCOREBOARD_SCORE_LENGTH_1_7 = 16;

    /** Player name length limit for scoreboard score on 1.8 and higher */
    public static final int SCOREBOARD_SCORE_LENGTH_1_8 = 40;

    /** Max scoreboard title length for versions below 1.13 */
    public static final int SCOREBOARD_TITLE_PRE_1_13 = 32;

    /** Max team prefix/suffix length for versions below 1.13 */
    public static final int TEAM_PREFIX_SUFFIX_PRE_1_13 = 16;

    /** Max tablist entry length for 1.7 and lower */
    public static final int MAX_DISPLAY_NAME_LENGTH_1_7 = 16;
}
