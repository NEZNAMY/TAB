package me.neznamy.tab.platforms.bukkit.nms;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.StructuredComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Class for converting TAB component into NMS components (1.7+).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ComponentConverter {

    /** Instance of the class */
    @Nullable
    public static ComponentConverter INSTANCE;

    private final FunctionWithException<String, Object> newTextComponent;
    private final BiFunction<ChatModifier, Boolean, Object> convertModifier;

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
     * Constructs new instance and loads all NMS classes, constructors and methods.
     *
     * @throws  ReflectiveOperationException
     *          If something failed
     */
    private ComponentConverter() throws ReflectiveOperationException {
        Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
        if (BukkitReflection.getMinorVersion() >= 19) {
            Method IChatBaseComponent_b = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"b", "literal"}, String.class);
            newTextComponent = text -> IChatBaseComponent_b.invoke(null, text);
            Class<?> IChatMutableComponent = BukkitReflection.getClass("network.chat.MutableComponent", "network.chat.IChatMutableComponent", "IChatMutableComponent");
            Component_modifier = ReflectionUtils.getOnlyField(IChatMutableComponent, ChatModifier);
            ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(IChatMutableComponent, IChatMutableComponent, IChatBaseComponent);
        } else {
            Class<?> ChatComponentText = BukkitReflection.getClass("network.chat.TextComponent", "network.chat.ChatComponentText", "ChatComponentText");
            Constructor<?> newChatComponentText = ChatComponentText.getConstructor(String.class);
            newTextComponent = newChatComponentText::newInstance;
            Class<?> ChatBaseComponent = BukkitReflection.getClass("network.chat.BaseComponent", "network.chat.ChatBaseComponent", "ChatBaseComponent");
            Component_modifier = ReflectionUtils.getOnlyField(ChatBaseComponent, ChatModifier);
            ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(ChatComponentText, IChatBaseComponent, IChatBaseComponent);
        }
        if (BukkitReflection.getMinorVersion() >= 16) {
            Class<?> chatHexColor = BukkitReflection.getClass("network.chat.TextColor", "network.chat.ChatHexColor", "ChatHexColor");
            Class<?> ResourceLocation = BukkitReflection.getClass("resources.ResourceLocation", "resources.MinecraftKey", "MinecraftKey");
            Class<?> chatClickable = BukkitReflection.getClass("network.chat.ClickEvent", "network.chat.ChatClickable", "ChatClickable");
            Class<?> chatHoverable = BukkitReflection.getClass("network.chat.HoverEvent", "network.chat.ChatHoverable", "ChatHoverable");
            ResourceLocation_tryParse = ReflectionUtils.getMethod(ResourceLocation, new String[]{"tryParse", "m_135820_", "a"}, String.class);
            ChatHexColor_fromRGB = ReflectionUtils.getMethods(chatHexColor, chatHexColor, int.class).get(0); // There should only be 1, but some mods add more
            newChatModifier = ReflectionUtils.setAccessible(ChatModifier.getDeclaredConstructor(chatHexColor, Boolean.class, Boolean.class, Boolean.class,
                    Boolean.class, Boolean.class, chatClickable, chatHoverable, String.class, ResourceLocation));
            convertModifier = this::createModifierModern;
        } else {
            newChatModifier = ChatModifier.getConstructor();
            ChatModifier_setColor = ReflectionUtils.getOnlyMethod(ChatModifier, ChatModifier, EnumChatFormat);
            convertModifier = (modifier, protocolVersion) -> createModifierLegacy(modifier);
        }
    }

    /**
     * Converts TAB component to NMS component.
     *
     * @param   component
     *          Component to convert
     * @param   modern
     *          Whether client supports RGB or not
     * @return  Converted component
     */
    @SneakyThrows
    public Object convert(@NotNull TabComponent component, boolean modern) {
        if (component instanceof SimpleComponent) return newTextComponent.apply(((SimpleComponent) component).getText());

        StructuredComponent component1 = (StructuredComponent) component;
        Object nmsComponent = newTextComponent.apply(component1.getText());
        Component_modifier.set(nmsComponent, convertModifier.apply(component1.getModifier(), modern));
        for (StructuredComponent extra : component1.getExtra()) {
            ChatBaseComponent_addSibling.invoke(nmsComponent, convert(extra, modern));
        }
        return nmsComponent;
    }

    @SneakyThrows
    private Object createModifierModern(@NotNull ChatModifier modifier, boolean modern) {
        Object color = null;
        if (modifier.getColor() != null) {
            if (modern) {
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
                null,
                null,
                null,
                modifier.getFont() == null ? null : ResourceLocation_tryParse.invoke(null, modifier.getFont())
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
        return nmsModifier;
    }

    /**
     * Attempts to load component converter.
     */
    public static void tryLoad() {
        try {
            INSTANCE = new ComponentConverter();
        } catch (Exception ignored) {
        }
    }

    /**
     * Makes sure the component converter is available and throws an exception if not.
     */
    public static void ensureAvailable() {
        if (INSTANCE == null) throw new IllegalStateException("Component converter is not available");
    }
}
