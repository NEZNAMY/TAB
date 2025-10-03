package me.neznamy.tab.shared.features.layout;

import me.neznamy.tab.shared.config.skin.SkinManager;
import me.neznamy.tab.shared.platform.TabList.Skin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Skin manager for layout feature.
 */
public class LayoutSkinManager {

    /** Skin manager to load skins with */
    @NotNull private final SkinManager skinManager;

    /** Configured default skin */
    @Nullable private final Skin defaultSkin;

    /** Default skins per slot */
    @NotNull private final Map<Integer, Skin> defaultSkinHashMap = new HashMap<>();

    /**
     * Constructs new instance with given parameters and loads cache.
     *
     * @param   skinManager
     *          Skin manager to load skins with
     * @param   defaultSkin
     *          Defined default skin
     * @param   defaultSkinHashMap
     *          Map of default skins per slot
     */
    public LayoutSkinManager(@NotNull SkinManager skinManager, @NotNull String defaultSkin, @NotNull Map<Integer, String> defaultSkinHashMap) {
        this.skinManager = skinManager;
        this.defaultSkin = getSkin(defaultSkin);
        for (Entry<Integer, String> entry : defaultSkinHashMap.entrySet()) {
            Skin skin = getSkin(entry.getValue());
            if (skin != null) this.defaultSkinHashMap.put(entry.getKey(), skin);
        }
    }

    /**
     * Returns default skin of specified slot. If not defined, default skin is returned.
     *
     * @param   slot
     *          Slot id
     * @return  Default skin for specified slot
     */
    @Nullable
    public Skin getDefaultSkin(int slot) {
        return defaultSkinHashMap.getOrDefault(slot, defaultSkin);
    }

    /**
     * Returns skin from given skin definition. If Skin is invalid, warn is printed and
     * {@code null} is returned.
     *
     * @param   skin
     *          Full skin definition
     * @return  Skin from given definition
     */
    @Nullable
    public Skin getSkin(@NotNull String skin) {
        Skin skinObj = skinManager.getSkin(skin);
        if (skinObj != null) return skinObj;
        return defaultSkin;
    }
}