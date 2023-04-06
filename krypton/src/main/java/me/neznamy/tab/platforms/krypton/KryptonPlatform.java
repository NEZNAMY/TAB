package me.neznamy.tab.platforms.krypton;

import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.kryptonmc.api.Server;
import org.kryptonmc.api.entity.player.Player;
import org.kryptonmc.api.plugin.PluginContainer;

import java.util.Locale;

public class KryptonPlatform extends BackendPlatform {
    
    private final KryptonTAB plugin;
    private final Server server;

    public KryptonPlatform(KryptonTAB plugin) {
        this.plugin = plugin;
        server = plugin.getServer();
    }

    @Override
    public void sendConsoleMessage(String message, boolean translateColors) {
        Component object = translateColors ? LegacyComponentSerializer.legacyAmpersand().deserialize(message) : Component.text(message);
        Component actualMessage = Component.text().append(Component.text("[TAB] ")).append(object).build();
        server.getConsole().sendMessage(actualMessage);
    }

    @Override
    public void registerUnknownPlaceholder(String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> "");
    }

    @Override
    public String getPluginVersion(String plugin) {
        PluginContainer container = server.getPluginManager().getPlugin(plugin.toLowerCase(Locale.ROOT));
        return container == null ? null : container.getDescription().version();
    }

    @Override
    public void loadPlayers() {
        for (Player player : server.getPlayers()) {
            TAB.getInstance().addPlayer(new KryptonTabPlayer(player, plugin.getProtocolVersion(player)));
        }
    }

    @Override
    public void registerPlaceholders() {
        new KryptonPlaceholderRegistry(plugin).registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public PipelineInjector getPipelineInjector() {
        return null;
    }

    @Override
    public NameTag getUnlimitedNametags() {
        return new NameTag();
    }

    @Override
    public @NotNull TabExpansion getTabExpansion() {
        return new EmptyTabExpansion();
    }

    @Override
    public TabFeature getPerWorldPlayerlist() {
        return null;
    }
}
