package me.neznamy.tab.shared.features.redis;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class holding information about a player connected to another proxy.
 */
@Getter
@Setter
@AllArgsConstructor
public class RedisPlayer {

    /** Tablist UUID of the player */
    @NotNull
    private UUID uniqueId;

    /** Player's real name */
    @NotNull
    private String name;

    /** Player's name as seen in game profile */
    @NotNull
    private String nickname;

    /** Name of server the player is connected to */
    @NotNull
    private String server;

    /** Whether player is vanished or not */
    private boolean vanished;

    /** Whether player is staff or not */
    private boolean staff;
}