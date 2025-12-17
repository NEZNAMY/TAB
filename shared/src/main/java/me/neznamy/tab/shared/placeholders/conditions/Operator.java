package me.neznamy.tab.shared.placeholders.conditions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.function.QuadFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Class representing comparison operators.
 */
@RequiredArgsConstructor
public enum Operator {

    // Numbers
    GREATER_THAN_OR_EQUAL(">=", "LESS_THAN", (left, right, viewer, target) ->
            left.parseAsNumber(viewer, target) >= right.parseAsNumber(viewer, target)),
    GREATER_THAN(">", "LESS_THAN_OR_EQUAL", (left, right, viewer, target) ->
            left.parseAsNumber(viewer, target) > right.parseAsNumber(viewer, target)),
    LESS_THAN_OR_EQUAL("<=", "GREATER_THAN", (left, right, viewer, target) ->
            left.parseAsNumber(viewer, target) <= right.parseAsNumber(viewer, target)),
    LESS_THAN("<", "GREATER_THAN_OR_EQUAL", (left, right, viewer, target) ->
            left.parseAsNumber(viewer, target) < right.parseAsNumber(viewer, target)),
    
    // String
    NOT_CONTAINS("!<-", "CONTAINS", (left, right, viewer, target) ->
            !left.parse(viewer, target).contains(right.parse(viewer, target))),
    NOT_STARTS_WITH("!|-", "STARTS_WITH", (left, right, viewer, target) ->
            !left.parse(viewer, target).startsWith(right.parse(viewer, target))),
    NOT_ENDS_WITH("!-|", "ENDS_WITH", (left, right, viewer, target) ->
            !left.parse(viewer, target).endsWith(right.parse(viewer, target))),
    CONTAINS("<-", "NOT_CONTAINS", (left, right, viewer, target) ->
            left.parse(viewer, target).contains(right.parse(viewer, target))),
    STARTS_WITH("|-", "NOT_STARTS_WITH", (left, right, viewer, target) ->
            left.parse(viewer, target).startsWith(right.parse(viewer, target))),
    ENDS_WITH("-|", "NOT_ENDS_WITH", (left, right, viewer, target) ->
            left.parse(viewer, target).endsWith(right.parse(viewer, target))),
    NOT_EQUALS("!=", "EQUALS", (left, right, viewer, target) ->
            !left.parse(viewer, target).equals(right.parse(viewer, target))),
    EQUALS("=", "NOT_EQUALS", (left, right, viewer, target) ->
            left.parse(viewer, target).equals(right.parse(viewer, target)));

    /** Symbol representing the operator */
    @Getter
    @NotNull
    private final String symbol;

    /** Opposite operator symbol, saved as string because we cannot reference constants before creating them */
    @Getter
    @NotNull
    private final String opposite;

    /** Function to execute the operator logic */
    @NotNull
    private final QuadFunction<ConditionSide, ConditionSide, TabPlayer, TabPlayer, Boolean> function;

    /**
     * Evaluates the operator with given sides and players.
     *
     * @param   left
     *          Left side of the condition
     * @param   right
     *          Right side of the condition
     * @param   viewer
     *          Viewer player
     * @param   target
     *          Target player
     * @return  {@code true} if the condition is met, {@code false} otherwise
     */
    public boolean evaluate(@NotNull ConditionSide left, @NotNull ConditionSide right, @NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        return function.apply(left, right, viewer, target);
    }
}
