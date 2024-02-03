package me.neznamy.tab.platforms.fabric;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.neznamy.tab.platforms.fabric.features.FabricNameTagX;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Platform implementation for Fabric
 */
@RequiredArgsConstructor
@Getter
public class FabricPlatform implements BackendPlatform {

    /** Minecraft server reference */
    private final MinecraftServer server;

    /** Flag tracking presence of permission API */
    private final boolean fabricPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    /** Server version */
    private final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(FabricTAB.minecraftVersion);

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        registerDummyPlaceholder(identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            TAB.getInstance().addPlayer(new FabricTabPlayer(this, player));
        }
    }

    @Override
    @NotNull
    public PipelineInjector createPipelineInjector() {
        return new FabricPipelineInjector();
    }

    @Override
    @NotNull
    public NameTag getUnlimitedNameTags() {
        return new FabricNameTagX();
    }

    @Override
    @NotNull
    public TabExpansion createTabExpansion() {
        return new EmptyTabExpansion();
    }

    @Override
    @Nullable
    public TabFeature getPerWorldPlayerList() {
        return null;
    }

    @Override
    @SneakyThrows
    public void logInfo(@NotNull TabComponent message) {
        Object logger = getLogger();
        logger.getClass().getMethod("info", String.class).invoke(logger, "[TAB] " + message.toLegacyText());
    }

    @Override
    @SneakyThrows
    public void logWarn(@NotNull TabComponent message) {
        Object logger = getLogger();
        logger.getClass().getMethod("warn", String.class).invoke(logger, "[TAB] " + message.toLegacyText());
    }

    @SneakyThrows
    private Object getLogger() {
        Class<?> loggerClass;
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_18_2.getNetworkId()) {
            loggerClass = Class.forName("org.slf4j.Logger");
        } else {
            loggerClass = Class.forName("org.apache.logging.log4j.Logger");
        }
        return ReflectionUtils.getFields(MinecraftServer.class, loggerClass).get(0).get(null);
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[Fabric] " + FabricTAB.minecraftVersion;
    }

    @Override
    public void registerListener() {
        new FabricEventListener().register();
    }

    @Override
    public void registerCommand() {
        // Event listener must be registered in main class
    }

    @Override
    public void startMetrics() {
        // Not available
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve(TabConstants.PLUGIN_ID).toFile();
    }

    /**
     * Converts internal component class to platform's component class
     *
     * @param   component
     *          Component to convert
     * @param   version
     *          Game version to convert component for
     * @return  Converted component
     */
    public Component toComponent(@NotNull TabComponent component, @NotNull ProtocolVersion version) {
        return FabricMultiVersion.deserialize.apply(component.toString(version));
    }

    @Override
    public double getTPS() {
        return -1; // Not available
    }

    @Override
    public double getMSPT() {
        return FabricMultiVersion.getMSPT.apply(server);
    }

    /**
     * Checks for permission and returns the result.
     *
     * @param   source
     *          Source to check permission of
     * @param   permission
     *          Permission node to check
     * @return  {@code true} if has permission, {@code false} if not
     */
    public boolean hasPermission(@NotNull CommandSourceStack source, @NotNull String permission) {
        if (source.hasPermission(4)) return true;
        return fabricPermissionsApi && Permissions.check(source, permission);
    }
}
