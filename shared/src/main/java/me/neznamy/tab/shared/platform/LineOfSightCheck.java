package me.neznamy.tab.shared.platform;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A viewer and target whose block line of sight must be checked.
 * Results are returned in the same order as the submitted checks.
 */
@Getter
@RequiredArgsConstructor
public class LineOfSightCheck {

    @NotNull private final TabPlayer viewer;
    @NotNull private final TabPlayer target;
}
