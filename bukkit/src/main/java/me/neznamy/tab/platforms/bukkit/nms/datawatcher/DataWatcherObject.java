package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

/**
 * A class representing the n.m.s.DataWatcherObject class to make work with it much easier
 */
public class DataWatcherObject {

    /** Object's position in DataWatcher */
    private final int position;
    
    /** Value encoder for 1.9+ */
    private final Object serializer;

    /**
     * Constructs a new instance of this class with given parameters
     *
     * @param   position
     *          position in DataWatcher
     * @param   serializer
     *          Serializer for value in this slot
     */
    public DataWatcherObject(int position, Object serializer){
        this.position = position;
        this.serializer = serializer;
    }

    /**
     * Returns {@link #position}
     * @return  {@link #position}
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns {@link #serializer}
     * @return  {@link #serializer}
     */
    public Object getSerializer() {
        return serializer;
    }

    @Override
    public String toString() {
        return String.format("DataWatcherObject{position=%d,serializer=%s}", position, serializer);
    }
}