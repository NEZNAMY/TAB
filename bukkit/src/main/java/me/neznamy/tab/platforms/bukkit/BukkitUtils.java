package me.neznamy.tab.platforms.bukkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Class containing static util methods used on Bukkit.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BukkitUtils {

    /**
     * Returns online players from Bukkit API. This requires reflection, as return type changed in 1.8,
     * and we want to avoid errors.
     *
     * @return  Online players from Bukkit API.
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    @NotNull
    public static Player[] getOnlinePlayers() {
        Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
        if (players instanceof Player[]) {
            //1.7-
            return (Player[]) players;
        } else {
            //1.8+
            return ((Collection<Player>)players).toArray(new Player[0]);
        }
    }

    /**
     * Converts component to legacy string using bukkit RGB format if supported by both server and client.
     * If not, closest legacy color is used instead.
     *
     * @param   component
     *          Component to convert
     * @param   rgbClient
     *          Whether client accepts RGB colors or not.
     * @return  Converted string using bukkit color format
     */
    @NotNull
    public static String toBukkitFormat(@NotNull IChatBaseComponent component, boolean rgbClient) {
        StringBuilder sb = new StringBuilder();
        if (component.getModifier().getColor() != null) {
            if (BukkitReflection.getMinorVersion() >= 16 && rgbClient) {
                String hexCode = component.getModifier().getColor().getHexCode();
                char c = EnumChatFormat.COLOR_CHAR;
                sb.append(c).append("x").append(c).append(hexCode.charAt(0)).append(c).append(hexCode.charAt(1))
                        .append(c).append(hexCode.charAt(2)).append(c).append(hexCode.charAt(3))
                        .append(c).append(hexCode.charAt(4)).append(c).append(hexCode.charAt(5));
            } else {
                sb.append(component.getModifier().getColor().getLegacyColor().getFormat());
            }
        }
        if (component.getModifier().isBold()) sb.append(EnumChatFormat.BOLD.getFormat());
        if (component.getModifier().isStrikethrough()) sb.append(EnumChatFormat.STRIKETHROUGH.getFormat());
        if (component.getModifier().isItalic()) sb.append(EnumChatFormat.ITALIC.getFormat());
        if (component.getModifier().isObfuscated()) sb.append(EnumChatFormat.OBFUSCATED.getFormat());
        if (component.getModifier().isUnderlined()) sb.append(EnumChatFormat.UNDERLINE.getFormat());
        if (component.getText() != null) sb.append(component.getText());
        for (IChatBaseComponent extra : component.getExtra()) {
            sb.append(toBukkitFormat(extra, rgbClient));
        }
        return sb.toString();
    }
}
