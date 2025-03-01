package me.neznamy.chat.hook;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.ComponentUtil;
import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.TextColor;
import me.neznamy.chat.component.KeybindComponent;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.chat.component.TranslatableComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class ViaVersionHook {

    @NotNull
    public static Object convert(@NotNull TabComponent component) {
        if (component instanceof SimpleTextComponent) {
            return ComponentUtil.legacyToJson(component.toLegacyText());
        } else if (component instanceof TextComponent) {
            return convert((TextComponent) component);
        } else if (component instanceof TranslatableComponent) {
            return convert((TranslatableComponent) component);
        } else if (component instanceof KeybindComponent) {
            return convert((KeybindComponent) component);
        } else {
            throw new IllegalStateException("Unknown component type: " + component.getClass().getName());
        }
    }

    @NotNull
    private static Object convert(@NotNull TextComponent component) {
        final JsonObject object = new JsonObject();

        // Root
        object.addProperty("type", "text"); // +1.20.3 optimization
        object.addProperty("text", component.getText());

        // Color
        final ChatModifier modifier = component.getModifier();
        if (modifier.getColor() != null) {
            final TextColor color = modifier.getColor();
            if (color.isLegacy()) {
                object.addProperty("color", color.getLegacyColor().name().toLowerCase());
            } else {
                object.addProperty("color", "#" + color.getHexCode());
            }
        }
        if (modifier.getShadowColor() != null) {
            object.addProperty("shadow_color", modifier.getShadowColor());
        }

        // Render
        if (Boolean.TRUE.equals(modifier.getBold())) {
            object.addProperty("bold", true);
        }
        object.addProperty("italic", Boolean.TRUE.equals(modifier.getItalic()));
        if (Boolean.TRUE.equals(modifier.getObfuscated())) {
            object.addProperty("obfuscated", true);
        }
        if (Boolean.TRUE.equals(modifier.getStrikethrough())) {
            object.addProperty("strikethrough", true);
        }
        if (Boolean.TRUE.equals(modifier.getUnderlined())) {
            object.addProperty("underlined", true);
        }
        if (modifier.getFont() != null) {
            object.addProperty("font", modifier.getFont());
        }

        // Extra
        if (!component.getExtra().isEmpty()) {
            final JsonArray extra = new JsonArray();
            for (TabComponent sub : component.getExtra()) {
                extra.add((JsonElement) convert(sub));
            }
            object.add("extra", extra);
        }

        return object;
    }

    @NotNull
    private static Object convert(@NotNull TranslatableComponent component) {
        final JsonObject object = new JsonObject();

        // Root
        object.addProperty("type", "translatable"); // +1.20.3 optimization
        object.addProperty("translate", component.getKey());

        // With
        if (!component.getExtra().isEmpty()) {
            final JsonArray extra = new JsonArray();
            for (TabComponent sub : component.getExtra()) {
                extra.add((JsonElement) convert(sub));
            }
            object.add("with", extra);
        }

        return object;
    }

    @NotNull
    private static Object convert(@NotNull KeybindComponent component) {
        final JsonObject object = new JsonObject();

        // Root
        object.addProperty("type", "keybind"); // +1.20.3 optimization
        object.addProperty("keybind", component.getKeybind());

        return object;
    }
}
