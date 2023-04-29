package me.neznamy.tab.shared.features.redis;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class RedisPlayer {

    @NonNull private UUID uniqueId;
    @NonNull private String name;
    @NonNull private String nickname;
    @NonNull private String server;
    private boolean vanished;
    private boolean staff;
}