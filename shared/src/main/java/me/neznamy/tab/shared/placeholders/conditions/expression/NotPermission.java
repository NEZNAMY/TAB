package me.neznamy.tab.shared.placeholders.conditions.expression;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An expression that checks if a player does not have a specific permission.
 */
@RequiredArgsConstructor
public class NotPermission extends ConditionalExpression {

    /** Permission to check */
    private final String permission;

    @Override
    public boolean isMet(@NonNull TabPlayer p) {
        return !p.hasPermission(permission);
    }

    @Override
    @NotNull
    public ConditionalExpression invert() {
        return new Permission(permission);
    }

    @Override
    @NotNull
    public String toShortFormat() {
        return "!permission:" + permission;
    }
}
