package me.neznamy.tab.shared.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A class representing a click action in components.
 */
@RequiredArgsConstructor
@Getter
public class TabClickEvent {

    /** Click action */
    @NotNull
    private final Action action;

    /** Value associated with the action */
    @NotNull
    private final String value;

    /**
     * Enum representing possible click actions.
     */
    public enum Action {

        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
        SHOW_DIALOG,
        COPY_TO_CLIPBOARD,
        CUSTOM,
    }
}
