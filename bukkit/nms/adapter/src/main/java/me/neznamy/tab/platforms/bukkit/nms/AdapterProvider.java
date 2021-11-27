package me.neznamy.tab.platforms.bukkit.nms;

import org.bukkit.Bukkit;

public final class AdapterProvider {

    private static final String BASE_PACKAGE = "me.neznamy.tab.platforms.bukkit.nms";
    private static final String VERSION = getVersion();
    private static final int MINOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);
    private static final Adapter ADAPTER = selectAdapter();

    public static Adapter get() {
        return ADAPTER;
    }

    public static String getLoadedVersion() {
        return VERSION;
    }

    public static int getMinorVersion() {
        return MINOR_VERSION;
    }

    private static Adapter selectAdapter() {
        try {
            final Class<?> adapterClass = Class.forName(BASE_PACKAGE + VERSION + ".AdapterImpl");
            return (Adapter) adapterClass.getConstructor().newInstance();
        } catch (final Exception exception) {
            // ignored
        }
        throw new RuntimeException("Could not find NMS adapter for version " + VERSION);
    }

    private static String getVersion() {
        final String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private AdapterProvider() {
    }
}
