package me.neznamy.chat.hook;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.ComponentUtil;
import me.neznamy.chat.ChatModifier;
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
        }

        final JsonObject object = new JsonObject();

        // Root
        if (component instanceof TextComponent) {
            object.addProperty("type", "text"); // +1.20.3 optimization
            object.addProperty("text", ((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            object.addProperty("type", "translatable"); // +1.20.3 optimization
            object.addProperty("translate", ((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            object.addProperty("type", "keybind"); // +1.20.3 optimization
            object.addProperty("keybind", ((KeybindComponent) component).getKeybind());
        } else {
            throw new IllegalStateException("Unknown component type: " + component.getClass().getName());
        }

        // Color
        final ChatModifier modifier = component.getModifier();
        if (modifier.getColor() != null) {
            object.addProperty("color", "#" + modifier.getColor().getHexCode());
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
}
