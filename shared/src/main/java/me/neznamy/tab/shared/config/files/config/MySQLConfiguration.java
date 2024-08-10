package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MySQLConfiguration extends ConfigurationSection {

    private final String SECTION = "mysql";
    @NotNull public final String host = getString(SECTION + ".host", "127.0.0.1");
    public final int port = getInt(SECTION + ".port", 3306);
    @NotNull public final String database = getString(SECTION + ".database", "tab");
    @NotNull public final String username = getString(SECTION + ".username", "user");
    @NotNull public final String password = getString(SECTION + ".password", "password");
    public final boolean useSSL = getBoolean(SECTION + ".useSSL", true);

    public MySQLConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "host", "port", "database", "username", "password", "useSSL"));
    }
}
