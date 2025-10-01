package me.neznamy.tab.platforms.bukkit.provider;

import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.TabKeybindComponent;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.chat.component.TabTranslatableComponent;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Interface for converting TAB components into NMS components (1.7+).
 *
 * @param   <T>
 *          NMS component type
 */
public abstract class ComponentConverter<T> {

    /** Empty UUID for creating dummy profiles */
    protected final UUID NIL_UUID = new UUID(0, 0);

    /**
     * Converts TAB component to NMS component.
     *
     * @param   component
     *          Component to convert
     * @return  Converted component
     */
    @NotNull
    public T convert(@NotNull TabComponent component) {
        // Component type
        T nmsComponent;
        if (component instanceof TabTextComponent) {
            nmsComponent = newTextComponent(((TabTextComponent) component).getText());
        } else if (component instanceof TabTranslatableComponent) {
            nmsComponent = newTranslatableComponent(((TabTranslatableComponent) component).getKey());
        } else if (component instanceof TabKeybindComponent) {
            nmsComponent = newKeybindComponent(((TabKeybindComponent)component).getKeybind());
        } else if (component instanceof TabObjectComponent) {
            if ((((TabObjectComponent) component).getContents() instanceof TabAtlasSprite)) {
                nmsComponent = newObjectComponent((TabAtlasSprite) ((TabObjectComponent) component).getContents());
            } else if ((((TabObjectComponent) component).getContents() instanceof TabPlayerSprite)) {
                nmsComponent = newObjectComponent((TabPlayerSprite) ((TabObjectComponent) component).getContents());
            } else {
                throw new IllegalArgumentException("Unexpected object component type: " + ((TabObjectComponent) component).getContents().getClass().getName());
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
    public abstract T newTextComponent(@NotNull String text);

    /**
     * Creates a new translatable component with the given key.
     *
     * @param   key
     *          Key to translate
     * @return  Translatable component with the given key
     */
    @NotNull
    public abstract T newTranslatableComponent(@NotNull String key);

    /**
     * Creates a new keybind component with given keybind.
     *
     * @param   keybind
     *          Keybind to show
     * @return  Keybind component with given keybind
     */
    @NotNull
    public abstract T newKeybindComponent(@NotNull String keybind);

    /**
     * Creates a new object component with given atlas and sprite.
     *
     * @param sprite Sprite to use
     * @return Object component with given atlas and sprite
     */
    @NotNull
    public abstract T newObjectComponent(@NotNull TabAtlasSprite sprite);

    /**
     * Creates a new head component with given skin.
     *
     * @param   sprite
     *          Skin to use
     * @return  Head component with given skin
     */
    @NotNull
    public abstract T newObjectComponent(@NotNull TabPlayerSprite sprite);

    /**
     * Converts given chat modifier to minecraft style and applies it to the component.
     *
     * @param   nmsComponent
     *          Component to apply style to
     * @param   style
     *          Style to convert and apply
     */
    public abstract void applyStyle(@NotNull T nmsComponent, @NotNull TabStyle style);

    /**
     * Appends child to the given parent component.
     *
     * @param   parent
     *          Parent to append the child to
     * @param   child
     *          Child component to append
     */
    public abstract void addSibling(@NotNull T parent, @NotNull T child);
}
