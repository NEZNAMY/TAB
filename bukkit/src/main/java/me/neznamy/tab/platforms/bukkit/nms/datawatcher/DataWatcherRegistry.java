package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.nms.storage.NMSStorage;

import java.util.Map;

/**
 * A class representing the n.m.s.DataWatcherRegistry class to make work with it much easier
 */
public class DataWatcherRegistry {

    /** Byte encoder */
    @Getter private Object typeByte;

    /** Float encoder */
    @Getter private Object typeFloat;

    /** String encoder */
    @Getter private Object typeString;

    /** Encoder for optional component */
    @Getter private Object typeOptionalComponent;

    /** Boolean encoder */
    @Getter private Object typeBoolean;

    /**
     * Initializes required NMS classes and fields
     */
    public DataWatcherRegistry(NMSStorage nms) {
        if (nms.getMinorVersion() >= 9) {
            Map<String, Object> fields = nms.getStaticFields(nms.DataWatcherRegistry);
            if (fields.containsKey("a")) {
                // Bukkit mapping
                typeByte = fields.get("a");
                typeFloat = fields.get("c");
                typeString = fields.get("d");
                if (nms.getMinorVersion() >= 13) {
                    if (nms.is1_19_3Plus()) {
                        typeOptionalComponent = fields.get("g");
                        typeBoolean = fields.get("j");
                    } else {
                        typeOptionalComponent = fields.get("f");
                        typeBoolean = fields.get("i");
                    }
                } else {
                    typeBoolean = fields.get("h");
                }
            } else {
                // Mojang mapping
                typeByte = fields.get("BYTE");
                typeFloat = fields.get("FLOAT");
                typeString = fields.get("STRING");
                typeOptionalComponent = fields.get("OPTIONAL_COMPONENT");
                typeBoolean = fields.get("BOOLEAN");
            }
        }
    }
}