package me.neznamy.tab.shared.placeholders.conditions.expression;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An expression that checks if a player has a specific permission.
 */
@RequiredArgsConstructor
public class Permission extends ConditionalExpression {

    /** Permission to check */
    private final String permission;

    @Override
    public boolean isMet(@NonNull TabPlayer p) {
        return p.hasPermission(permission);
    }

    @Override
    @NotNull
    public ConditionalExpression invert() {
        return new NotPermission(permission);
    }

    @Override
    @NotNull
    public String toShortFormat() {
        return "permission:" + permission;
    }
}
