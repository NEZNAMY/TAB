package me.neznamy.chat.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.EnumChatFormat;
import me.neznamy.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A component of "text" type that contains text to display.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TextComponent extends TabComponent {

    @NotNull
    protected String text;

    /**
     * Constructs new instance using given text and extra components.
     *
     * @param   text
     *          Component text
     * @param   extra
     *          Extra components
     */
    public TextComponent(@NotNull String text, List<TabComponent> extra) {
        this.text = text;
        super.extra = extra;
    }

    /**
     * Constructs a new instance which clones existing component.
     *
     * @param   component
     *          Component to clone
     */
    public TextComponent(@NotNull TextComponent component) {
        text = component.text;
        modifier = new ChatModifier(component.modifier);
    }

    /**
     * Constructs new instance with given text and color.
     *
     * @param   text
     *          Component text
     * @param   color
     *          Text color
     */
    public TextComponent(@NotNull String text, @Nullable TextColor color) {
        this.text = text;
        modifier.setColor(color);
    }

    @Override
    @NotNull
    public String toLegacyText() {
        StringBuilder builder = new StringBuilder();
        append(builder, "");
        return builder.toString();
    }

    /**
     * Appends text to string builder, might also add color and magic codes if they are different
     * from the previous component in the chain.
     *
     * @param   builder
     *          builder to append text to
     * @param   previousFormatting
     *          colors and magic codes in previous component
     * @return  the new formatting, it might be identical to the previous one
     */
    @NotNull
    private String append(@NotNull StringBuilder builder, @NotNull String previousFormatting) {
        String formatting = getFormatting();
        if (!formatting.equals(previousFormatting)) {
            builder.append(formatting);
        }
        builder.append(text);
        for (TabComponent component : getExtra()) {
            if (component instanceof TextComponent) {
                formatting = ((TextComponent) component).append(builder, formatting);
            }
        }
        return formatting;
    }

    /**
     * Returns colors and magic codes of this component
     *
     * @return  used colors and magic codes
     */
    private @NotNull String getFormatting() {
        StringBuilder builder = new StringBuilder();
        if (modifier.getColor() != null) {
            builder.append("§");
            if (modifier.getColor().getLegacyColor() == EnumChatFormat.WHITE) {
                //preventing unwanted &r → &f conversion and stopping the <1.13 client bug fix from working
                builder.append("r");
            } else {
                builder.append(modifier.getColor().getLegacyColor().getCharacter());
            }
        }
        builder.append(modifier.getMagicCodes());
        return builder.toString();
    }
}
