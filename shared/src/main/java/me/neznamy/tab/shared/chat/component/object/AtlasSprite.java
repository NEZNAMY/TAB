package me.neznamy.tab.shared.chat.component.object;

import lombok.Data;
import lombok.NonNull;

/**
 * Object type representing an atlas and sprite.
 */
@Data
public class AtlasSprite implements ObjectInfo {

    @NonNull
    private final String atlas;

    @NonNull
    private final String sprite;
}