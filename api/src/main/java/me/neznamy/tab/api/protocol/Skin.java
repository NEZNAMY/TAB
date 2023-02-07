package me.neznamy.tab.api.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * Class representing a minecraft skin as a value - signature pair.
 */
@Data @AllArgsConstructor
public class Skin {

    /** Skin value */
    @NonNull private final String value;

    /** Skin signature, can be null */
    private final String signature;
}
