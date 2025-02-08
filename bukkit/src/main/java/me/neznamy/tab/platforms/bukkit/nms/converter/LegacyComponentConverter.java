package me.neznamy.tab.platforms.bukkit.nms.converter;

import lombok.SneakyThrows;
import me.neznamy.chat.ChatModifier;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Class for converting TAB components into NMS components for versions 1.7 - 1.15.2.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class LegacyComponentConverter extends ComponentConverter {

    private final Class<?> IChatBaseComponent = BukkitReflection.getClass("IChatBaseComponent");

    private final Constructor<?> newTextComponent = BukkitReflection.getClass("ChatComponentText").getConstructor(String.class);
    private final Constructor<?> newTranslatableComponent = BukkitReflection.getClass("ChatMessage").getConstructor(String.class, Object[].class);
    private Constructor<?> newKeybindComponent;

    private final Class<?> ChatModifierClass = BukkitReflection.getClass("ChatModifier");
    private final Constructor<?> newChatModifier = ChatModifierClass.getConstructor();
    private final Class<Enum> EnumChatFormat = (Class<Enum>) BukkitReflection.getClass("ChatFormatting", "EnumChatFormat");
    private final Class<?> ChatBaseComponent = BukkitReflection.getClass("ChatBaseComponent");
    private final Method ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(ChatBaseComponent, IChatBaseComponent, IChatBaseComponent);
    private final Field Component_modifier = ReflectionUtils.getOnlyField(ChatBaseComponent, ChatModifierClass);
    private final List<Field> magicCodes = ReflectionUtils.getFields(ChatModifierClass, Boolean.class);
    private final Method ChatModifier_setColor = ReflectionUtils.getOnlyMethod(ChatModifierClass, ChatModifierClass, EnumChatFormat);

    /**
     * Constructs new instance and loads all NMS classes, constructors, and methods.
     *
     * @throws  ReflectiveOperationException
     *          If something failed
     */
    public LegacyComponentConverter() throws ReflectiveOperationException {
        if (BukkitReflection.getMinorVersion() >= 12) {
            newKeybindComponent = BukkitReflection.getClass("ChatComponentKeybind").getConstructor(String.class);
        }
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
        if (BukkitReflection.getMinorVersion() >= 12) {
            return newKeybindComponent.newInstance(keybind);
        }
        throw new UnsupportedOperationException("Keybind components were added in 1.12");
    }

    @Override
    @SneakyThrows
    public void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier style) {
        Object nmsModifier = newChatModifier.newInstance();
        if (style.getColor() != null) {
            ChatModifier_setColor.invoke(nmsModifier, Enum.valueOf(EnumChatFormat, style.getColor().getLegacyColor().name()));
        }
        magicCodes.get(0).set(nmsModifier, style.getBold());
        magicCodes.get(1).set(nmsModifier, style.getItalic());
        magicCodes.get(2).set(nmsModifier, style.getStrikethrough());
        magicCodes.get(3).set(nmsModifier, style.getUnderlined());
        magicCodes.get(4).set(nmsModifier, style.getObfuscated());
        Component_modifier.set(nmsComponent, nmsModifier);
    }

    @Override
    @SneakyThrows
    public void addSibling(@NotNull Object parent, @NotNull Object child) {
        ChatBaseComponent_addSibling.invoke(parent, child);
    }
}
