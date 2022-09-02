package me.neznamy.tab.platforms.bukkit.nms;

import java.util.Arrays;

/**
 * NMS loader for minecraft 1.17+ using Mojang packaging.
 */
public abstract class ModernNMSStorage extends NMSStorage {

    /** Additional classes used in 1.19+ */
    protected Class<?> IRegistry;
    protected Class<?> Registry;

    /**
     * Creates new instance, initializes required NMS classes and fields
     *
     * @throws  ReflectiveOperationException
     *          If any class, field or method fails to load
     */
    public ModernNMSStorage() throws ReflectiveOperationException {
    }

    /**
     * Returns class with given potential names in same order
     *
     * @param   names
     *          possible class names
     * @return  class for specified names
     * @throws  ClassNotFoundException
     *          if class does not exist
     */
    protected Class<?> getModernClass(String... names) throws ClassNotFoundException {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                //not the first class name in array
            }
        }
        throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
    }
}
