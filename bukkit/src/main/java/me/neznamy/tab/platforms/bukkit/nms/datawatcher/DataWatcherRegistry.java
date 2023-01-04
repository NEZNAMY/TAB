package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import me.neznamy.tab.platforms.bukkit.nms.storage.NMSStorage;

import java.util.Map;

/**
 * A class representing the n.m.s.DataWatcherRegistry class to make work with it much easier
 */
public class DataWatcherRegistry {

    /** Byte encoder */
    private Object registryByte;

    /** Float encoder */
    private Object registryFloat;

    /** String encoder */
    private Object registryString;

    /** Encoder for optional component */
    private Object registryOptionalIChatBaseComponent;

    /** Boolean encoder */
    private Object registryBoolean;

    /**
     * Initializes required NMS classes and fields
     */
    public DataWatcherRegistry(NMSStorage nms) {
        if (nms.getMinorVersion() >= 9) {
            Map<String, Object> fields = nms.getStaticFields(nms.DataWatcherRegistry);
            if (fields.containsKey("a")) {
                // Bukkit mapping
                registryByte = fields.get("a");
                registryFloat = fields.get("c");
                registryString = fields.get("d");
                if (nms.getMinorVersion() >= 13) {
                    if (nms.is1_19_3Plus()) {
                        registryOptionalIChatBaseComponent = fields.get("g");
                        registryBoolean = fields.get("j");
                    } else {
                        registryOptionalIChatBaseComponent = fields.get("f");
                        registryBoolean = fields.get("i");
                    }
                } else {
                    registryBoolean = fields.get("h");
                }
            } else {
                // Mojang mapping
                registryByte = fields.get("BYTE");
                registryFloat = fields.get("FLOAT");
                registryString = fields.get("STRING");
                registryOptionalIChatBaseComponent = fields.get("OPTIONAL_COMPONENT");
                registryBoolean = fields.get("BOOLEAN");
            }
        }
    }

    /**
     * Returns {@link #registryByte}
     * @return  {@link #registryByte}
     */
    public Object getByte() {
        return registryByte;
    }

    /**
     * Returns {@link #registryFloat}
     * @return  {@link #registryFloat}
     */
    public Object getFloat() {
        return registryFloat;
    }

    /**
     * Returns {@link #registryString}
     * @return  {@link #registryString}
     */
    public Object getString() {
        return registryString;
    }

    /**
     * Returns {@link #registryOptionalIChatBaseComponent}
     * @return  {@link #registryOptionalIChatBaseComponent}
     */
    public Object getOptionalComponent() {
        return registryOptionalIChatBaseComponent;
    }

    /**
     * Returns {@link #registryBoolean}
     * @return  {@link #registryBoolean}
     */
    public Object getBoolean() {
        return registryBoolean;
    }
}