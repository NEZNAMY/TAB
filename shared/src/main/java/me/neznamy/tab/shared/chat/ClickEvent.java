package me.neznamy.tab.shared.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class ClickEvent {

    @NotNull private final Action action;
    @NotNull private final String value;

    public enum Action {

        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE, //since 1.8
        COPY_TO_CLIPBOARD //since 1.15
    }
}
