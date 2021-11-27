package me.neznamy.tab.platforms.bukkit.nms.v1_17_R1;

import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;
import net.minecraft.network.syncher.EntityDataSerializers;

public final class DataWatcherRegistryImpl implements DataWatcherRegistry {

    @Override
    public Object getBoolean() {
        return EntityDataSerializers.BOOLEAN;
    }

    @Override
    public Object getByte() {
        return EntityDataSerializers.BYTE;
    }

    @Override
    public Object getInteger() {
        return EntityDataSerializers.INT;
    }

    @Override
    public Object getFloat() {
        return EntityDataSerializers.FLOAT;
    }

    @Override
    public Object getString() {
        return EntityDataSerializers.STRING;
    }

    @Override
    public Object getOptionalComponent() {
        return EntityDataSerializers.OPTIONAL_COMPONENT;
    }
}
