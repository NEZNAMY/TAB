package me.neznamy.tab.platforms.krypton;

import lombok.NonNull;
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
import org.jetbrains.annotations.Nullable;
import org.kryptonmc.api.Server;
import org.kryptonmc.api.entity.player.Player;

public class KryptonPlatform extends BackendPlatform {
    
    @NonNull private final KryptonTAB plugin;
    @NonNull private final Server server;

    public KryptonPlatform(@NonNull KryptonTAB plugin) {
        this.plugin = plugin;
        server = plugin.getServer();
    }

    @Override
    public void sendConsoleMessage(@NonNull String message, boolean translateColors) {
        Component object = translateColors ? LegacyComponentSerializer.legacyAmpersand().deserialize(message) : Component.text(message);
        Component actualMessage = Component.text().append(Component.text("[TAB] ")).append(object).build();
        server.getConsole().sendMessage(actualMessage);
    }

    @Override
    public void registerUnknownPlaceholder(@NonNull String identifier) {
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
        new KryptonPlaceholderRegistry(plugin).registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public @Nullable PipelineInjector getPipelineInjector() {
        return null;
    }

    @Override
    public @NotNull NameTag getUnlimitedNametags() {
        return new NameTag();
    }

    @Override
    public @NotNull TabExpansion getTabExpansion() {
        return new EmptyTabExpansion();
    }

    @Override
    public @Nullable TabFeature getPerWorldPlayerlist() {
        return null;
    }
}
