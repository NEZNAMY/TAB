package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;

import java.util.Map;

/**
 * A class representing the n.m.s.DataWatcherRegistry class to make work with it much easier
 */
public class DataWatcherRegistry {

    private Object registryByte;
    private Object registryFloat;
    private Object registryString;
    private Object registryOptionalIChatBaseComponent;
    private Object registryBoolean;

    /**
     * Initializes required NMS classes and fields
     */
    public DataWatcherRegistry(NMSStorage nms) {
        if (nms.getMinorVersion() >= 9) {
            Map<String, Object> fields = nms.getStaticFields(nms.DataWatcherRegistry);
            registryByte = fields.get("a");
            registryFloat = fields.get("c");
            registryString = fields.get("d");
            if (nms.getMinorVersion() >= 13) {
                registryOptionalIChatBaseComponent = fields.get("f");
                registryBoolean = fields.get("i");
            } else {
                registryBoolean = fields.get("h");
            }
        }
    }

    public Object getByte() {
        return registryByte;
    }

    public Object getFloat() {
        return registryFloat;
    }

    public Object getString() {
        return registryString;
    }

    public Object getOptionalComponent() {
        return registryOptionalIChatBaseComponent;
    }

    public Object getBoolean() {
        return registryBoolean;
    }
}