package me.neznamy.tab.shared.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing servers and worlds, as well as other data related to them.
 */
@Getter
public class DataManager {

    /** Map of all servers, indexed by their name */
    private final Map<String, Server> servers = new HashMap<>();

    /** Map of all worlds, indexed by their name */
    private final Map<String, World> worlds = new HashMap<>();
}
