package me.neznamy.tab.platforms.bukkit;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class ServerVersionInfo {

    /** Package name of the server implementation, null on Paper 1.20.5+ / Spigot 26+ */
    @Nullable
    private final String serverPackage;

    /** Minecraft version as returned by the platform */
    @NotNull
    private final String minecraftVersion;

    @NotNull
    private final String serverName;

    /** Server version */
    private final ProtocolVersion serverVersion;

    /** Implementation for creating new instances using content available on the server */
    @NotNull
    @Setter
    private ImplementationProvider implementationProvider;

    /**
     * Constructs new instance and detects server version info.
     */
    public ServerVersionInfo() {
        // Server package
        String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
        String[] array = CRAFTBUKKIT_PACKAGE.split("\\.");
        serverPackage = array.length > 3 ? array[3] : null;

        // Minecraft version
        if (ReflectionUtils.methodExists(Bukkit.class, "getMinecraftVersion")) {
            serverName = "Paper";
            minecraftVersion = Bukkit.getMinecraftVersion();
        } else {
            serverName = "Spigot";
            minecraftVersion = Bukkit.getBukkitVersion().split("-")[0];
        }

        // Minecraft version to server version
        serverVersion = ProtocolVersion.fromFriendlyName(minecraftVersion);

        // Find implementation
        implementationProvider = findImplementationProvider();
    }

    /**
     * Finds implementation provider for current server software and version.
     *
     * @return  Implementation provider for current server
     * @throws  IllegalStateException
     *          If no implementation was found
     */
    @NotNull
    private ImplementationProvider findImplementationProvider() {
        if (serverPackage != null) {
            // Paper <1.20.5 or Spigot 1.x
            try {
                // Does not actually support flat 1.19, but whatever, no one is using it anyway
                return (ImplementationProvider) Class.forName("me.neznamy.tab.platforms.bukkit." + serverPackage + ".NMSImplementationProvider").getConstructor().newInstance();
            } catch (ReflectiveOperationException ignored) {
                throw new IllegalStateException(String.format(
                        "Your server version (%s - %s) is no longer supported.",
                        minecraftVersion, serverPackage
                ));
            }
        }

        // Paper 1.20.5+ or Spigot 26+
        Map<ProtocolVersion, String> spigotVersions = new LinkedHashMap<>();
        spigotVersions.put(ProtocolVersion.V26_1, "v26_1");
        spigotVersions.put(ProtocolVersion.V26_1_1, "v26_1");

        Map<ProtocolVersion, String> paperVersions = new LinkedHashMap<>();
        paperVersions.put(ProtocolVersion.V1_20_5, "paper_1_20_5");
        paperVersions.put(ProtocolVersion.V1_20_6, "paper_1_20_5");
        paperVersions.put(ProtocolVersion.V1_21, "paper_1_20_5");
        paperVersions.put(ProtocolVersion.V1_21_1, "paper_1_20_5");
        paperVersions.put(ProtocolVersion.V1_21_2, "paper_1_21_2");
        paperVersions.put(ProtocolVersion.V1_21_3, "paper_1_21_2");
        paperVersions.put(ProtocolVersion.V1_21_4, "paper_1_21_4");
        paperVersions.put(ProtocolVersion.V1_21_5, "paper_1_21_4");
        paperVersions.put(ProtocolVersion.V1_21_6, "paper_1_21_4");
        paperVersions.put(ProtocolVersion.V1_21_7, "paper_1_21_4");
        paperVersions.put(ProtocolVersion.V1_21_8, "paper_1_21_4");
        paperVersions.put(ProtocolVersion.V1_21_9, "paper_1_21_9");
        paperVersions.put(ProtocolVersion.V1_21_10, "paper_1_21_9");
        paperVersions.put(ProtocolVersion.V1_21_11, "paper_1_21_11");
        paperVersions.put(ProtocolVersion.V26_1, "paper_1_21_11");  // v26_1 works too
        paperVersions.put(ProtocolVersion.V26_1_1, "paper_1_21_11");  // v26_1 works too

        if (serverVersion == ProtocolVersion.UNKNOWN) {
            throw new IllegalStateException(String.format(
                    "Unknown server version (%s), can not find implementation.",
                    minecraftVersion
            ));
        }
        String implementation = (serverName.equals("Paper") ? paperVersions : spigotVersions).get(serverVersion);
        if (implementation == null) {
            throw new IllegalStateException(String.format(
                    "Your server version (%s %s) is no longer supported.",
                    serverName, minecraftVersion
            ));
        }
        try {
            return (ImplementationProvider) Class.forName("me.neznamy.tab.platforms.bukkit." + implementation + ".NMSImplementationProvider").getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(String.format(
                    "Your server version (%s %s) is marked as compatible, but the implementation does not exist. This is probably a bug.",
                    serverName, minecraftVersion
            ), e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(String.format(
                    "Failed to initialize implementation for %s %s. This is probably a bug.",
                    serverName, minecraftVersion
            ), e);
        }
    }
}
