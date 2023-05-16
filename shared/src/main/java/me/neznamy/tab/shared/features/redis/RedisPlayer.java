package me.neznamy.tab.shared.features.redis;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class RedisPlayer {

    @NotNull private UUID uniqueId;
    @NotNull private String name;
    @NotNull private String nickname;
    @NotNull private String server;
    private boolean vanished;
    private boolean staff;
}