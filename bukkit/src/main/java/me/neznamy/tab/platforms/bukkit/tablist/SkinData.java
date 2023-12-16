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

public class SkinData {

    private final Method getHandle;
    private final Method getProfile;

    @SneakyThrows
    public SkinData() {
        Class<?> EntityHuman = BukkitReflection.getClass("world.entity.player.Player", "world.entity.player.EntityHuman", "EntityHuman");
        getHandle = BukkitReflection.getBukkitClass("entity.CraftPlayer").getMethod("getHandle");
        // There is only supposed to be one, however there are exceptions:
        // #1 - CatServer adds another method
        // #2 - Random mods may perform deep hack into the server and add another one (see #1089)
        // Get first and hope for the best, alternatively players may not have correct skins in layout, but who cares
        getProfile = ReflectionUtils.getMethods(EntityHuman, GameProfile.class).get(0);
    }

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