package me.neznamy.tab.shared.chat.hook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A separate class for handling shadow color in adventure components.
 * It is required to be separate. Using static functions on interfaces causes
 * it to load on class initialization even if the code never runs, resulting in an error
 * when using an older version of the library before shadow color class was added.
 */
public class AdventureShadowHook {

    /**
     * Returns shadow color of the component in ARGB format or {@code null} if not defined.
     *
     * @param   component
     *          Component to get shadow color of
     * @return  Shadow color of the component in ARGB format or {@code null} if not defined
     */
    @Nullable
    public static Integer getShadowColor(@NotNull Component component) {
        ShadowColor color = component.shadowColor();
        return color == null ? null : color.value();
    }

    /**
     * Sets shadow color for the style in AGB format if it is not {@code null}.
     *
     * @param   style
     *          Style to set shadow color of
     * @param   shadowColor
     *          Shadow color in ARGB format or {@code null} for default
     */
    public static void setShadowColor(@NotNull Style.Builder style, @Nullable Integer shadowColor) {
        if (shadowColor != null) {
            style.shadowColor(ShadowColor.shadowColor(shadowColor));
        }
    }
}