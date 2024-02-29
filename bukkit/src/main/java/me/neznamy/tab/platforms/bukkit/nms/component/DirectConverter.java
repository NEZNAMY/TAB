package me.neznamy.tab.platforms.bukkit.nms.component;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.*;
import me.neznamy.tab.shared.util.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Component converter using direct component conversion.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DirectConverter {

    private final FunctionWithException<String, Object> newTextComponent;

    private final Class<?> ChatModifier = BukkitReflection.getClass("network.chat.ChatModifier", "ChatModifier");
    private final Class<Enum> EnumChatFormat = (Class<Enum>) BukkitReflection.getClass("EnumChatFormat");
    private final Class<?> ChatClickable = BukkitReflection.getClass("network.chat.ChatClickable", "ChatClickable");
    private final Class<Enum> EnumClickAction = (Class<Enum>) BukkitReflection.getClass("network.chat.ChatClickable$EnumClickAction", "ChatClickable$EnumClickAction", "EnumClickAction");

    private final Constructor<?> newChatClickable = ChatClickable.getConstructor(EnumClickAction, String.class);
    private final Constructor<?> newChatModifier;

    private final Method ChatModifier_setClickEvent = ReflectionUtils.getOnlyMethod(ChatModifier, ChatModifier, ChatClickable);
    private final Method ChatBaseComponent_addSibling;

    private final Field Component_modifier;
    private final List<Field> magicCodes = ReflectionUtils.getFields(ChatModifier, Boolean.class);

    // 1.15-
    private Method ChatModifier_setColor;

    // 1.16+
    private Method ChatHexColor_fromRGB;
    private Constructor<?> newMinecraftKey;

    /**
     * Constructs new instance and loads all NMS classes, constructors and methods.
     *
     * @throws  ReflectiveOperationException
     *          If something failed
     */
    public DirectConverter() throws ReflectiveOperationException {
        Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.IChatBaseComponent", "IChatBaseComponent");
        if (BukkitReflection.getMinorVersion() >= 19) {
            Method IChatBaseComponent_b = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"b", "literal"}, String.class);
            newTextComponent = text -> IChatBaseComponent_b.invoke(null, text);
            Class<?> IChatMutableComponent = BukkitReflection.getClass("network.chat.IChatMutableComponent", "IChatMutableComponent");
            Component_modifier = ReflectionUtils.getOnlyField(IChatMutableComponent, ChatModifier);
            ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(IChatMutableComponent, IChatMutableComponent, IChatBaseComponent);
        } else {
            Class<?> ChatComponentText = BukkitReflection.getClass("network.chat.ChatComponentText", "ChatComponentText");
            Constructor<?> newChatComponentText = ChatComponentText.getConstructor(String.class);
            newTextComponent = newChatComponentText::newInstance;
            Class<?> ChatBaseComponent = BukkitReflection.getClass("network.chat.ChatBaseComponent", "ChatBaseComponent");
            Component_modifier = ReflectionUtils.getOnlyField(ChatBaseComponent, ChatModifier);
            ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(ChatComponentText, IChatBaseComponent, IChatBaseComponent);
        }
        if (BukkitReflection.getMinorVersion() >= 16) {
            Class<?> chatHexColor = BukkitReflection.getClass("network.chat.ChatHexColor", "ChatHexColor");
            Class<?> MinecraftKey = BukkitReflection.getClass("resources.MinecraftKey", "MinecraftKey");
            Class<?> chatHoverable = BukkitReflection.getClass("network.chat.ChatHoverable", "ChatHoverable");
            newMinecraftKey = MinecraftKey.getConstructor(String.class);
            ChatHexColor_fromRGB = ReflectionUtils.getOnlyMethod(chatHexColor, chatHexColor, int.class);
            newChatModifier = ReflectionUtils.setAccessible(ChatModifier.getDeclaredConstructor(chatHexColor, Boolean.class, Boolean.class, Boolean.class,
                    Boolean.class, Boolean.class, ChatClickable, chatHoverable, String.class, MinecraftKey));
        } else {
            newChatModifier = ChatModifier.getConstructor();
            ChatModifier_setColor = ReflectionUtils.getOnlyMethod(ChatModifier, ChatModifier, EnumChatFormat);
        }
    }

    /**
     * Converts TAB component to NMS component.
     *
     * @param   component
     *          Component to convert
     * @param   version
     *          Client version
     * @return  Converted component
     */
    @SneakyThrows
    public Object convert(@NotNull TabComponent component, @NotNull ProtocolVersion version) {
        if (component instanceof SimpleComponent) return newTextComponent.apply(((SimpleComponent) component).getText());

        StructuredComponent component1 = (StructuredComponent) component;
        Object nmsComponent = newTextComponent.apply(component1.getText());
        if (BukkitReflection.getMinorVersion() >= 16) {
            Component_modifier.set(nmsComponent, createModifierModern(component1.getModifier(), version));
        } else {
            Component_modifier.set(nmsComponent, createModifierLegacy(component1.getModifier()));
        }
        for (StructuredComponent extra : component1.getExtra()) {
            ChatBaseComponent_addSibling.invoke(nmsComponent, convert(extra, version));
        }
        return nmsComponent;
    }

    @SneakyThrows
    private Object createModifierModern(@NotNull ChatModifier modifier, @NotNull ProtocolVersion clientVersion) {
        Object color = null;
        if (modifier.getColor() != null) {
            if (clientVersion.supportsRGB()) {
                color = ChatHexColor_fromRGB.invoke(null, modifier.getColor().getRgb());
            } else {
                color = ChatHexColor_fromRGB.invoke(null, modifier.getColor().getLegacyColor().getRgb());
            }
        }
        return newChatModifier.newInstance(
                color,
                modifier.isBold(),
                modifier.isItalic(),
                modifier.isUnderlined(),
                modifier.isStrikethrough(),
                modifier.isObfuscated(),
                modifier.getClickEvent() == null ? null : newChatClickable.newInstance(Enum.valueOf(EnumClickAction, modifier.getClickEvent().getAction().name()), modifier.getClickEvent().getValue()),
                null,
                null,
                modifier.getFont() == null ? null : newMinecraftKey.newInstance(modifier.getFont())
        );
    }

    @SneakyThrows
    private Object createModifierLegacy(@NotNull ChatModifier modifier) {
        Object nmsModifier = newChatModifier.newInstance();
        if (modifier.getColor() != null) {
            ChatModifier_setColor.invoke(nmsModifier, Enum.valueOf(EnumChatFormat, modifier.getColor().getLegacyColor().name()));
        }
        if (modifier.isBold()) magicCodes.get(0).set(nmsModifier, true);
        if (modifier.isItalic()) magicCodes.get(1).set(nmsModifier, true);
        if (modifier.isStrikethrough()) magicCodes.get(2).set(nmsModifier, true);
        if (modifier.isUnderlined()) magicCodes.get(3).set(nmsModifier, true);
        if (modifier.isObfuscated()) magicCodes.get(4).set(nmsModifier, true);
        ClickEvent event = modifier.getClickEvent();
        if (event != null) {
            ChatModifier_setClickEvent.invoke(nmsModifier, newChatClickable.newInstance(Enum.valueOf(EnumClickAction, event.getAction().name()), event.getValue()));
        }
        return nmsModifier;
    }
}
