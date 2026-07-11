package me.neznamy.tab.platforms.fand;

import io.fand.api.Fand;
import io.fand.api.command.CommandRegistration;
import io.fand.api.entity.Player;
import io.fand.api.plugin.PluginContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import me.neznamy.tab.shared.ProjectVariables;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.impl.AdventureBossBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Platform implementation backed exclusively by Fand's stable plugin API. */
public final class FandPlatform implements BackendPlatform {

    private static final Pattern LEGACY_FORMATTING = Pattern.compile("(?i)§(?:#[0-9a-f]{6}|[0-9a-fk-orx])");

    private final PluginContext context;
    private final List<CommandRegistration> customCommands = new ArrayList<>();
    private final int protocolVersion;

    public FandPlatform(@NotNull PluginContext context) {
        this.context = context;
        ProtocolVersion version = ProtocolVersion.fromFriendlyName(Fand.server().minecraftVersion());
        protocolVersion = version == ProtocolVersion.UNKNOWN
                ? ProtocolVersion.V26_2.getNetworkId()
                : version.getNetworkId();
    }

    PluginContext context() {
        return context;
    }

    int protocolVersion() {
        return protocolVersion;
    }

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        if (identifier.startsWith("%rel_")) {
            registerDummyPlaceholder(identifier);
            return;
        }
        String fandIdentifier = identifier;
        if (identifier.length() > 2 && identifier.startsWith("%") && identifier.endsWith("%")) {
            fandIdentifier = identifier.substring(1, identifier.length() - 1);
        }
        String resolvedIdentifier = fandIdentifier;
        PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
        if (identifier.startsWith("%server_")) {
            manager.registerServerPlaceholder(identifier, () -> context.placeholders()
                    .resolve(null, resolvedIdentifier)
                    .orElse(identifier));
            return;
        }
        manager.registerPlayerPlaceholder(identifier, player -> context.placeholders()
                .resolve(((FandTabPlayer) player).getPlayer(), resolvedIdentifier)
                .orElse(identifier));
    }

    @Override
    public void loadPlayers() {
        for (Player player : Fand.server().players()) {
            TAB.getInstance().addPlayer(new FandTabPlayer(this, player));
        }
    }

    @Override
    @Nullable
    public PipelineInjector createPipelineInjector() {
        return null;
    }

    @Override
    @Nullable
    public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
        return null;
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        context.logger().info("[TAB] {}", plainText(message));
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        context.logger().warn("[TAB] {}", plainText(message));
    }

    @Override
    public void registerListener() {
        new FandEventListener(context).register();
    }

    @Override
    public void registerCommand() {
        new FandTabCommand(getCommand()).register(context.commands());
    }

    @Override
    public void startMetrics() {
        // Fand does not currently provide a metrics integration.
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return context.dataDirectory().toFile();
    }

    @Override
    @NotNull
    public Object convertComponent(@NotNull TabComponent component) {
        return component.toAdventure();
    }

    @Override
    @NotNull
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        return new FandScoreboard((FandTabPlayer) player);
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NotNull TabPlayer player) {
        return new AdventureBossBar(player);
    }

    @Override
    @NotNull
    public TabList createTabList(@NotNull TabPlayer player) {
        return new FandTabList((FandTabPlayer) player, context);
    }

    @Override
    public boolean supportsScoreboards() {
        return true;
    }

    @Override
    public void registerCustomCommand(@NotNull String commandName,
                                      @NotNull BiConsumer<TabPlayer, String[]> function) {
        CommandRegistration registration = new FandCommand(commandName) {
            @Override
            protected void execute(@NotNull io.fand.api.command.CommandContext command,
                                   @NotNull String[] arguments) {
                if (!(command.sender() instanceof Player player)) {
                    command.sender().sendMessage(TabComponent.fromColoredText(TAB.getInstance()
                            .getConfiguration().getMessages().getCommandOnlyFromGame()).toAdventure());
                    return;
                }
                TabPlayer tabPlayer = TAB.getInstance().getPlayer(player.uniqueId());
                if (tabPlayer != null) {
                    function.accept(tabPlayer, arguments);
                }
            }
        }.register(context.commands());
        synchronized (customCommands) {
            customCommands.add(registration);
        }
    }

    @Override
    public void unregisterAllCustomCommands() {
        synchronized (customCommands) {
            customCommands.forEach(CommandRegistration::unregister);
            customCommands.clear();
        }
    }

    @Override
    public double getTPS() {
        return Math.min(20.0, Fand.server().performance().oneSecond().ticksPerSecond().average());
    }

    @Override
    public double getMSPT() {
        return Fand.server().performance().currentMillisecondsPerTick();
    }

    @Override
    public void runSyncGlobal(@NotNull Runnable task) {
        context.scheduler().runMain(task);
    }

    @Override
    public boolean hasLineOfSight(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        return ((FandTabPlayer) viewer).getPlayer().lineOfSight(((FandTabPlayer) target).getPlayer());
    }

    @Override
    @NotNull
    public Object dump() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("server-type", Fand.server().brand());
        data.put("server-version", Fand.server().version());
        data.put("minecraft-version", Fand.server().minecraftVersion());
        data.put("tab-version", ProjectVariables.PLUGIN_VERSION);
        List<String> plugins = Fand.server().plugins().loaded().stream()
                .map(plugin -> plugin.getClass().getName())
                .sorted(Comparator.naturalOrder())
                .toList();
        data.put("plugins", plugins);
        return data;
    }

    private static String plainText(TabComponent message) {
        return LEGACY_FORMATTING.matcher(message.toLegacyText()).replaceAll("");
    }
}
