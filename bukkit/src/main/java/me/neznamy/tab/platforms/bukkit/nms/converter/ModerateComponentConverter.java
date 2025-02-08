package me.neznamy.tab.platforms.bukkit.nms.converter;

import lombok.SneakyThrows;
import me.neznamy.chat.ChatModifier;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class for converting TAB components into NMS components for versions 1.16 - 1.18.2.
 */
public class ModerateComponentConverter extends ComponentConverter {

    private final Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
    private final Class<?> ChatBaseComponent = BukkitReflection.getClass("network.chat.BaseComponent", "network.chat.ChatBaseComponent", "ChatBaseComponent");
    private final Class<?> TextColor = BukkitReflection.getClass("network.chat.TextColor", "network.chat.ChatHexColor", "ChatHexColor");
    private final Class<?> ResourceLocation = BukkitReflection.getClass("resources.ResourceLocation", "resources.MinecraftKey", "MinecraftKey");

    private final Constructor<?> newTextComponent = BukkitReflection.getClass("network.chat.TextComponent", "network.chat.ChatComponentText", "ChatComponentText").getConstructor(String.class);
    private final Constructor<?> newTranslatableComponent = BukkitReflection.getClass("network.chat.TranslatableComponent", "network.chat.ChatMessage", "ChatMessage").getConstructor(String.class, Object[].class);
    private final Constructor<?> newKeybindComponent = BukkitReflection.getClass("network.chat.KeybindComponent", "network.chat.ChatComponentKeybind", "ChatComponentKeybind").getConstructor(String.class);

    private final Class<?> ChatModifierClass = BukkitReflection.getClass("network.chat.Style", "network.chat.ChatModifier", "ChatModifier");
    private final Constructor<?> newChatModifier = ReflectionUtils.setAccessible(ChatModifierClass.getDeclaredConstructor(
            TextColor,
            Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class,
            BukkitReflection.getClass("network.chat.ClickEvent", "network.chat.ChatClickable", "ChatClickable"),
            BukkitReflection.getClass("network.chat.HoverEvent", "network.chat.ChatHoverable", "ChatHoverable"),
            String.class,
            ResourceLocation
    ));
    private final Method ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(ChatBaseComponent, IChatBaseComponent, IChatBaseComponent);
    private final Field Component_modifier = ReflectionUtils.getOnlyField(ChatBaseComponent, ChatModifierClass);

    private final Method ChatHexColor_fromRGB = ReflectionUtils.getMethods(TextColor, TextColor, int.class).get(0); // There should only be 1, but some mods add more
    private final Method ResourceLocation_tryParse = ReflectionUtils.getMethod(ResourceLocation, new String[]{"tryParse", "m_135820_", "a"}, String.class);

    /**
     * Constructs new instance and loads all NMS classes, constructors, and methods.
     *
     * @throws  ReflectiveOperationException
     *          If something failed
     */
    public ModerateComponentConverter() throws ReflectiveOperationException {
    }

    @Override
    @NotNull
    @SneakyThrows
    public Object newTextComponent(@NotNull String text) {
        return newTextComponent.newInstance(text);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Object newTranslatableComponent(@NotNull String key) {
        return newTranslatableComponent.newInstance(key, new Object[0]);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Object newKeybindComponent(@NotNull String keybind) {
        return newKeybindComponent.newInstance(keybind);
    }

    @Override
    @SneakyThrows
    public void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier style) {
        Component_modifier.set(nmsComponent, newChatModifier.newInstance(
                style.getColor() == null ? null : ChatHexColor_fromRGB.invoke(null, style.getColor().getRgb()),
                style.getBold(),
                style.getItalic(),
                style.getUnderlined(),
                style.getStrikethrough(),
                style.getObfuscated(),
                null,
                null,
                null,
                style.getFont() == null ? null : ResourceLocation_tryParse.invoke(null, style.getFont())
        ));
    }

    @Override
    @SneakyThrows
    public void addSibling(@NotNull Object parent, @NotNull Object child) {
        ChatBaseComponent_addSibling.invoke(parent, child);
    }
}
