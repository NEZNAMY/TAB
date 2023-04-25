package me.neznamy.tab.shared.features.redis;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class RedisPlayer {

    private UUID uniqueId;
    private String name;
    private String nickname;
    private String server;
    private boolean vanished;
    private boolean staff;
}