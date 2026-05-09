package me.neznamy.tab.shared.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a group of servers defined by name and patterns in global playerlist.
 */
@RequiredArgsConstructor
@Getter
public class ServerGroup {

    /** Default server group for all unlisted servers that should share playerlist */
    @NotNull
    public static final ServerGroup DEFAULT = new ServerGroup("<DEFAULT>", new ArrayList<>());

    /** Name of the server group as defined in configuration */
    @NonNull
    private final String name;

    /** Server definitions in this group with regex support if prefixed with "regex:" */
    @NonNull
    private final List<String> patterns;
}
