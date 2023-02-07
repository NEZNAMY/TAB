package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A class representing the n.m.s.DataWatcherObject class to make work with it much easier
 */
@Data @AllArgsConstructor
public class DataWatcherObject {

    /** Object's position in DataWatcher */
    private final int position;
    
    /** Value encoder for 1.9+ */
    private final Object serializer;
}