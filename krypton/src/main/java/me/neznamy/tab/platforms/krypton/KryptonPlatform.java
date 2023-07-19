package me.neznamy.tab.platforms.krypton;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kryptonmc.api.Server;
import org.kryptonmc.api.entity.player.Player;

public class KryptonPlatform implements BackendPlatform {
    
    @NotNull private final KryptonTAB plugin;
    @NotNull private final Server server;

    public KryptonPlatform(@NotNull KryptonTAB plugin) {
        this.plugin = plugin;
        server = plugin.getServer();
    }

    @Override
    public void logInfo(@NotNull IChatBaseComponent message) {
        server.getConsole().sendMessage(Component.text("[TAB] ").append(message.toAdventureComponent(TAB.getInstance().getServerVersion())));
    }

    @Override
    public void logWarn(@NotNull IChatBaseComponent message) {
        server.getConsole().sendMessage(Component.text(EnumChatFormat.RED.getFormat() + "[TAB] [WARN] ").append(message.toAdventureComponent(TAB.getInstance().getServerVersion())));
    }

    @Override
    public String getServerVersionInfo() {
        return "[Krypton] " + server.getPlatform().version();
    }

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> "");
    }

    @Override
    public void loadPlayers() {
        for (Player player : server.getPlayers()) {
            TAB.getInstance().addPlayer(new KryptonTabPlayer(player));
        }
    }

    @Override
    public void registerPlaceholders() {
        new KryptonPlaceholderRegistry(this, plugin).registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public @Nullable PipelineInjector createPipelineInjector() {
        return null;
    }

    @Override
    public @NotNull NameTag getUnlimitedNameTags() {
        return new NameTag();
    }

    @Override
    public @NotNull TabExpansion createTabExpansion() {
        return new EmptyTabExpansion();
    }

    @Override
    public @Nullable TabFeature getPerWorldPlayerList() {
        return null;
    }

    @Override
    public double getTPS() {
        return -1;
    }
}
