package me.neznamy.chat.component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A component of "object" type where currently the only implementation is atlas-sprite pair.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ObjectComponent extends TabComponent {

    /** Error message to display when this component is used on a server version lower than 1.21.9. */
    public static final String ERROR_MESSAGE = "<Object components were added in 1.21.9>";

    @NotNull
    protected final String atlas;

    @NotNull
    protected final String sprite;

    @Override
    @NotNull
    public String toLegacyText() {
        return ERROR_MESSAGE;
    }
}
