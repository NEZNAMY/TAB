package me.neznamy.tab.api.tablist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class representing a minecraft skin as a value - signature pair.
 */
@Data @AllArgsConstructor
public class Skin {

    /** Skin value */
    @NonNull private final String value;

    /** Skin signature */
    @Nullable private final String signature;
}
