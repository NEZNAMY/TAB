package me.neznamy.tab.platforms.bukkit.tablist;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Class for getting skin of players.
 */
public class SkinData {

    private final Method getHandle = BukkitReflection.getBukkitClass("entity.CraftPlayer").getMethod("getHandle");

    // There is only supposed to be one, however there are exceptions:
    // #1 - CatServer adds another method
    // #2 - Random mods may perform deep hack into the server and add another one (see #1089)
    // Get first and hope for the best, alternatively players may not have correct skins in layout, but who cares
    private final Method getProfile = ReflectionUtils.getMethods(BukkitReflection.getClass(
            "world.entity.player.Player", "world.entity.player.EntityHuman", "EntityHuman"), GameProfile.class).get(0);

    /**
     * Constructs new instance and loads NMS fields. If it fails, throws an Exception.
     *
     * @throws  ReflectiveOperationException
     *          If something goes wrong
     */
    public SkinData() throws ReflectiveOperationException {}

    /**
     * Returns player's skin. If server is in offline mode, returns {@code null}.
     *
     * @param   player
     *          Player to get skin of
     * @return  Player's skin or {@code null}.
     */
    @Nullable
    @SneakyThrows
    public TabList.Skin getSkin(@NotNull Player player) {
        Collection<Property> col = ((GameProfile) getProfile.invoke(getHandle.invoke(player))).getProperties().get(TabList.TEXTURES_PROPERTY);
        if (col.isEmpty()) return null; //offline mode
        Property property = col.iterator().next();
        if (BukkitReflection.is1_20_2Plus()) {
            return new TabList.Skin(
                    (String) property.getClass().getMethod("value").invoke(property),
                    (String) property.getClass().getMethod("signature").invoke(property)
            );
        } else {
            return new TabList.Skin(property.getValue(), property.getSignature());
        }
    }
}