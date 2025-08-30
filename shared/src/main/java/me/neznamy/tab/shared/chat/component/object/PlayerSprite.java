package me.neznamy.tab.shared.chat.component.object;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Sprite for player head it has 2 parameters: <p>
 * 1. GameProfile - UUID, name, skin <p>
 * 2. boolean - whether to show hat or not <p>
 * Any part of the profile can be used to display the head - UUID, name or properties. <p>
 * This class will support all of them.
 */
@Getter
public class PlayerSprite implements ObjectInfo {

    /** UUID of a player to show skin of */
    @Nullable
    private UUID id;

    /** Name of a player to show skin of */
    @Nullable
    private String name;

    /** Skin texture to show */
    @Nullable
    private TabList.Skin skin;

    /** Whether to show hat layer or not */
    @Setter
    private boolean showHat;

    /**
     * Constructs new instance for skin by UUID.
     *
     * @param   id
     *          UUID of a player to show skin of
     */
    public PlayerSprite(@NonNull UUID id) {
        this.id = id;
    }

    /**
     * Constructs new instance for skin by name.
     *
     * @param   name
     *          Name of a player to show skin of
     */
    public PlayerSprite(@NonNull String name) {
        this.name = name;
    }

    /**
     * Constructs new instance for specified skin.
     *
     * @param   skin
     *          Skin to show
     */
    public PlayerSprite(@NonNull TabList.Skin skin) {
        this.skin = skin;
    }
}
