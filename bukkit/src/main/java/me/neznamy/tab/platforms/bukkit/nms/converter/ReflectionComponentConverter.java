package me.neznamy.tab.platforms.bukkit.nms.converter;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.component.KeybindComponent;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TextComponent;
import me.neznamy.tab.shared.chat.component.TranslatableComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Class for converting TAB components into NMS components (1.7+).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ReflectionComponentConverter extends ComponentConverter {

    private final FunctionWithException<String, Object> newTextComponent;
    private final FunctionWithException<String, Object> newTranslatableComponent;
    private final FunctionWithException<String, Object> newKeybindComponent;

    private final Class<?> ChatModifier = BukkitReflection.getClass("network.chat.Style", "network.chat.ChatModifier", "ChatModifier");
    private final Class<Enum> EnumChatFormat = (Class<Enum>) BukkitReflection.getClass("ChatFormatting", "EnumChatFormat");
    private final Constructor<?> newChatModifier;
    private final Method ChatBaseComponent_addSibling;
    private final Field Component_modifier;
    private final List<Field> magicCodes = ReflectionUtils.getFields(ChatModifier, Boolean.class);

    // 1.15-
    private Method ChatModifier_setColor;

    // 1.16+
    private Method ChatHexColor_fromRGB;
    private Method ResourceLocation_tryParse;

    /**
     * Constructs new instance and loads all NMS classes, constructors, and methods.
     *
     * @throws  ReflectiveOperationException
     *          If something failed
     */
    public ReflectionComponentConverter() throws ReflectiveOperationException {
        Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
        if (BukkitReflection.getMinorVersion() >= 19) {
            Method IChatBaseComponent_b = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"b", "literal"}, String.class);
            newTextComponent = text -> IChatBaseComponent_b.invoke(null, text);

            Method IChatBaseComponent_c = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"c", "translatable"}, String.class);
            newTranslatableComponent = text -> IChatBaseComponent_c.invoke(null, text);

            Method IChatBaseComponent_d = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"d", "keybind"}, String.class);
            newKeybindComponent = text -> IChatBaseComponent_d.invoke(null, text);

            Class<?> IChatMutableComponent = BukkitReflection.getClass("network.chat.MutableComponent", "network.chat.IChatMutableComponent", "IChatMutableComponent");
            Component_modifier = ReflectionUtils.getOnlyField(IChatMutableComponent, ChatModifier);
            ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(IChatMutableComponent, IChatMutableComponent, IChatBaseComponent);
        } else {
            Class<?> ChatComponentText = BukkitReflection.getClass("network.chat.TextComponent", "network.chat.ChatComponentText", "ChatComponentText");
            Constructor<?> newChatComponentText = ChatComponentText.getConstructor(String.class);
            newTextComponent = newChatComponentText::newInstance;

            Class<?> ChatMessage = BukkitReflection.getClass("network.chat.TranslatableComponent", "network.chat.ChatMessage", "ChatMessage");
            Constructor<?> newChatMessage = ChatMessage.getConstructor(String.class, Object[].class);
            newTranslatableComponent = text -> newChatMessage.newInstance(text, new Object[0]);

            newKeybindComponent = text -> {
                throw new UnsupportedOperationException("Keybind component conversion is not implemented on < 1.19");
            };

            Class<?> ChatBaseComponent = BukkitReflection.getClass("network.chat.BaseComponent", "network.chat.ChatBaseComponent", "ChatBaseComponent");
            Component_modifier = ReflectionUtils.getOnlyField(ChatBaseComponent, ChatModifier);
            ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(ChatBaseComponent, IChatBaseComponent, IChatBaseComponent);
        }
        if (BukkitReflection.getMinorVersion() >= 16) {
            Class<?> chatHexColor = BukkitReflection.getClass("network.chat.TextColor", "network.chat.ChatHexColor", "ChatHexColor");
            Class<?> ResourceLocation = BukkitReflection.getClass("resources.ResourceLocation", "resources.MinecraftKey", "MinecraftKey");
            Class<?> chatClickable = BukkitReflection.getClass("network.chat.ClickEvent", "network.chat.ChatClickable", "ChatClickable");
            Class<?> chatHoverable = BukkitReflection.getClass("network.chat.HoverEvent", "network.chat.ChatHoverable", "ChatHoverable");
            ResourceLocation_tryParse = ReflectionUtils.getMethod(ResourceLocation, new String[]{"tryParse", "m_135820_", "a"}, String.class);
            ChatHexColor_fromRGB = ReflectionUtils.getMethods(chatHexColor, chatHexColor, int.class).get(0); // There should only be 1, but some mods add more
            if (BukkitReflection.is1_21_4Plus()) {
                // 1.21.4+
                newChatModifier = ReflectionUtils.setAccessible(ChatModifier.getDeclaredConstructor(chatHexColor, Integer.class, Boolean.class, Boolean.class,
                        Boolean.class, Boolean.class, Boolean.class, chatClickable, chatHoverable, String.class, ResourceLocation));
            } else {
                // 1.21.3-
                newChatModifier = ReflectionUtils.setAccessible(ChatModifier.getDeclaredConstructor(chatHexColor, Boolean.class, Boolean.class, Boolean.class,
                        Boolean.class, Boolean.class, chatClickable, chatHoverable, String.class, ResourceLocation));
            }
        } else {
            newChatModifier = ChatModifier.getConstructor();
            ChatModifier_setColor = ReflectionUtils.getOnlyMethod(ChatModifier, ChatModifier, EnumChatFormat);
        }
    }

    @Override
    @SneakyThrows
    @NotNull
    public Object convert(@NotNull TabComponent component) {
        // Component type
        Object nmsComponent;
        if (component instanceof TextComponent) {
            nmsComponent = newTextComponent.apply(((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            nmsComponent = newTranslatableComponent.apply(((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            nmsComponent = newKeybindComponent.apply(((KeybindComponent) component).getKeybind());
        } else {
            throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        Component_modifier.set(nmsComponent, convertStyle(component.getModifier()));

        // Extra
        for (TabComponent extra : component.getExtra()) {
            ChatBaseComponent_addSibling.invoke(nmsComponent, convert(extra));
        }
        return nmsComponent;
    }

    @SneakyThrows
    private Object convertStyle(@NotNull ChatModifier modifier) {
        if (BukkitReflection.is1_21_4Plus()) {
            return newChatModifier.newInstance(
                    modifier.getColor() == null ? null : ChatHexColor_fromRGB.invoke(null, modifier.getColor().getRgb()),
                    modifier.getShadowColor(),
                    modifier.getBold(),
                    modifier.getItalic(),
                    modifier.getUnderlined(),
                    modifier.getStrikethrough(),
                    modifier.getObfuscated(),
                    null,
                    null,
                    null,
                    modifier.getFont() == null ? null : ResourceLocation_tryParse.invoke(null, modifier.getFont())
            );
        } else if (BukkitReflection.getMinorVersion() >= 16) {
            return newChatModifier.newInstance(
                    modifier.getColor() == null ? null : ChatHexColor_fromRGB.invoke(null, modifier.getColor().getRgb()),
                    modifier.getBold(),
                    modifier.getItalic(),
                    modifier.getUnderlined(),
                    modifier.getStrikethrough(),
                    modifier.getObfuscated(),
                    null,
                    null,
                    null,
                    modifier.getFont() == null ? null : ResourceLocation_tryParse.invoke(null, modifier.getFont())
            );
        } else {
            Object nmsModifier = newChatModifier.newInstance();
            if (modifier.getColor() != null) {
                ChatModifier_setColor.invoke(nmsModifier, Enum.valueOf(EnumChatFormat, modifier.getColor().getLegacyColor().name()));
            }
            magicCodes.get(0).set(nmsModifier, modifier.getBold());
            magicCodes.get(1).set(nmsModifier, modifier.getItalic());
            magicCodes.get(2).set(nmsModifier, modifier.getStrikethrough());
            magicCodes.get(3).set(nmsModifier, modifier.getUnderlined());
            magicCodes.get(4).set(nmsModifier, modifier.getObfuscated());
            return nmsModifier;
        }
    }
}
