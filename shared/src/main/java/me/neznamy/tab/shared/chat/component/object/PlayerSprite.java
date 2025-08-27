package me.neznamy.tab.shared.chat.component.object;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabList;

/**
 * Sprite for player head it has 2 parameters: <p>
 * 1. GameProfile - UUID, name, skin <p>
 * 2. boolean - whether to show hat or not <p>
 * UUID and name seem to have zero effect, since it is just player head.
 */
@Data
public class PlayerSprite implements ObjectInfo {

    /** Skin to show */
    @NonNull
    private final TabList.Skin skin;

    /** Whether to show hat layer or not */
    private final boolean showHat;
}
