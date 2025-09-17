package me.neznamy.tab.shared.chat.component.object;

import lombok.*;
import me.neznamy.tab.shared.chat.component.TabComponent;
import org.jetbrains.annotations.NotNull;

/**
 * A component of "object" type.
 */
@Getter
@AllArgsConstructor
public class TabObjectComponent extends TabComponent {

    /** Error message to display when this component is used on a server version lower than 1.21.9. */
    public static final String ERROR_MESSAGE = "<Object components were added in 1.21.9>";

    /** Contents of the component */
    @NonNull
    protected final ObjectInfo contents;

    @Override
    @NotNull
    public String toLegacyText() {
        return ERROR_MESSAGE;
    }
}
