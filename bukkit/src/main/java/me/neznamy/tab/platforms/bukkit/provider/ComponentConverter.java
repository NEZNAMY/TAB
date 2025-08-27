package me.neznamy.tab.platforms.bukkit.provider;

import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.component.KeybindComponent;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TextComponent;
import me.neznamy.tab.shared.chat.component.TranslatableComponent;
import me.neznamy.tab.shared.chat.component.object.AtlasSprite;
import me.neznamy.tab.shared.chat.component.object.ObjectComponent;
import me.neznamy.tab.shared.chat.component.object.PlayerSprite;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for converting TAB components into NMS components (1.7+).
 */
public abstract class ComponentConverter {

    /**
     * Converts TAB component to NMS component.
     *
     * @param   component
     *          Component to convert
     * @return  Converted component
     */
    @NotNull
    public Object convert(@NotNull TabComponent component) {
        // Component type
        Object nmsComponent;
        if (component instanceof TextComponent) {
            nmsComponent = newTextComponent(((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            nmsComponent = newTranslatableComponent(((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            nmsComponent = newKeybindComponent(((KeybindComponent)component).getKeybind());
        } else if (component instanceof ObjectComponent) {
            if ((((ObjectComponent) component).getContents() instanceof AtlasSprite)) {
                nmsComponent = newObjectComponent((AtlasSprite) ((ObjectComponent) component).getContents());
            } else if ((((ObjectComponent) component).getContents() instanceof PlayerSprite)) {
                nmsComponent = newObjectComponent((PlayerSprite) ((ObjectComponent) component).getContents());
            } else {
                throw new IllegalArgumentException("Unexpected object component type: " + ((ObjectComponent) component).getContents().getClass().getName());
            }
        } else {
            throw new IllegalArgumentException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        applyStyle(nmsComponent, component.getModifier());

        // Extra
        for (TabComponent extra : component.getExtra()) {
            addSibling(nmsComponent, convert(extra));
        }

        return nmsComponent;
    }

    /**
     * Creates a new text component with given text.
     *
     * @param   text
     *          Text to display
     * @return  Text component with given text
     */
    @NotNull
    public abstract Object newTextComponent(@NotNull String text);

    /**
     * Creates a new translatable component with the given key.
     *
     * @param   key
     *          Key to translate
     * @return  Translatable component with the given key
     */
    @NotNull
    public abstract Object newTranslatableComponent(@NotNull String key);

    /**
     * Creates a new keybind component with given keybind.
     *
     * @param   keybind
     *          Keybind to show
     * @return  Keybind component with given keybind
     */
    @NotNull
    public abstract Object newKeybindComponent(@NotNull String keybind);

    /**
     * Creates a new object component with given atlas and sprite.
     *
     * @param sprite Sprite to use
     * @return Object component with given atlas and sprite
     */
    @NotNull
    public abstract Object newObjectComponent(@NotNull AtlasSprite sprite);

    /**
     * Creates a new head component with given skin.
     *
     * @param   sprite
     *          Skin to use
     * @return  Head component with given skin
     */
    @NotNull
    public abstract Object newObjectComponent(@NotNull PlayerSprite sprite);

    /**
     * Converts given chat modifier to minecraft style and applies it to the component.
     *
     * @param   nmsComponent
     *          Component to apply style to
     * @param   style
     *          Style to convert and apply
     */
    public abstract void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier style);

    /**
     * Appends child to the given parent component.
     *
     * @param   parent
     *          Parent to append the child to
     * @param   child
     *          Child component to append
     */
    public abstract void addSibling(@NotNull Object parent, @NotNull Object child);
}
