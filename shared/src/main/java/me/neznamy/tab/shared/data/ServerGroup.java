package me.neznamy.tab.shared.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Class representing a group of servers defined by name and patterns in global playerlist.
 */
@RequiredArgsConstructor
@Getter
public class ServerGroup {

    @NonNull private final String name;
    @NonNull private final List<String> patterns;
}
