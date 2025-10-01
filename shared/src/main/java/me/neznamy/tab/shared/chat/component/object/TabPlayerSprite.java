package me.neznamy.tab.shared.chat.component.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
@AllArgsConstructor
public class TabPlayerSprite implements ObjectInfo {

    /** UUID of a player to show skin of */
    @Nullable
    private final UUID id;

    /** Name of a player to show skin of */
    @Nullable
    private final String name;

    /** Skin texture to show */
    @Nullable
    private final TabList.Skin skin;

    /** Whether to show hat layer or not */
    private final boolean showHat;
}
