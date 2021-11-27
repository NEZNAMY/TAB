package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

public interface DataWatcherRegistry {

    Object getBoolean();

    Object getByte();

    Object getInteger();

    Object getFloat();

    Object getString();

    Object getOptionalComponent();
}
