package me.neznamy.chat.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A component of "keybind" type that contains bound key.
 */
@Getter
@AllArgsConstructor
public class KeybindComponent extends TabComponent {

    @NotNull
    protected final String keybind;

    @Override
    @NotNull
    public String toLegacyText() {
        return keybind;
    }
}
