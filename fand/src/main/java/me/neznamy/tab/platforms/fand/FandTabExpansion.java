package me.neznamy.tab.platforms.fand;

import io.fand.api.entity.Player;
import io.fand.api.placeholder.PlaceholderContext;
import io.fand.api.placeholder.PlaceholderProvider;
import io.fand.api.placeholder.PlaceholderRegistration;
import io.fand.api.plugin.PluginContext;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Exposes TAB's placeholder expansion through Fand's placeholder service. */
public final class FandTabExpansion implements TabExpansion {

    private static final String NAMESPACE_PREFIX = "tab_";
    private static final int MAX_REPLACEMENT_PASSES = 32;

    private final PluginContext context;
    private final PlaceholderRegistration registration;

    public FandTabExpansion(@NotNull PluginContext context) {
        this.context = context;
        registration = context.placeholders().register(
                "tab",
                PlaceholderProvider.contextual(this::resolve));
    }

    @Override
    public void unregisterExpansion() {
        registration.unregister();
    }

    @Nullable
    private String resolve(@NotNull PlaceholderContext placeholderContext, @NotNull String identifier) {
        if (!identifier.startsWith(NAMESPACE_PREFIX)) {
            return null;
        }
        String expansionIdentifier = identifier.substring(NAMESPACE_PREFIX.length());
        if (expansionIdentifier.startsWith("replace_")) {
            return resolveReplacement(placeholderContext, expansionIdentifier.substring("replace_".length()));
        }

        Player viewer = placeholderContext.viewer();
        Player target = placeholderContext.target();
        if (target != null) {
            return resolveRelational(viewer, target, expansionIdentifier);
        }
        if (viewer == null) {
            return "<Player cannot be null>";
        }
        TabPlayer player = TAB.getInstance().getPlayer(viewer.uniqueId());
        if (player == null) {
            return "<Player is not loaded>";
        }
        if (expansionIdentifier.startsWith("placeholder_")) {
            String requestedPlaceholder = "%" + expansionIdentifier.substring("placeholder_".length()) + "%";
            PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
            manager.addUsedPlaceholder(requestedPlaceholder, manager);
            return manager.getPlaceholder(requestedPlaceholder).parse(player);
        }
        return player.expansionData.getValue(expansionIdentifier);
    }

    @NotNull
    private String resolveRelational(
            @Nullable Player viewer,
            @NotNull Player target,
            @NotNull String expansionIdentifier
    ) {
        if (viewer == null) {
            return "<Player cannot be null>";
        }
        if (!expansionIdentifier.startsWith("placeholder_")) {
            return "<Unknown identifier: \"" + expansionIdentifier + "\">";
        }

        String requestedPlaceholder = "%" + expansionIdentifier.substring("placeholder_".length()) + "%";
        PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
        manager.addUsedPlaceholder(requestedPlaceholder, manager);
        Placeholder placeholder = manager.getPlaceholder(requestedPlaceholder);
        if (!(placeholder instanceof RelationalPlaceholderImpl relational)) {
            return "<Not a relational placeholder: " + requestedPlaceholder + ">";
        }

        TabPlayer tabViewer = TAB.getInstance().getPlayer(viewer.uniqueId());
        TabPlayer tabTarget = TAB.getInstance().getPlayer(target.uniqueId());
        if (tabViewer == null || tabTarget == null) {
            return "<Player is not loaded>";
        }
        return relational.getLastValue(tabViewer, tabTarget);
    }

    @NotNull
    private String resolveReplacement(
            @NotNull PlaceholderContext placeholderContext,
            @NotNull String requestedIdentifier
    ) {
        PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
        return expandReplacement("%" + requestedIdentifier + "%", text -> {
            String expanded = text;
            for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(text)) {
                String identifier = placeholder.substring(1, placeholder.length() - 1);
                String value = context.placeholders().resolve(identifier, placeholderContext).orElse(placeholder);
                expanded = expanded.replace(placeholder, manager.findReplacement(placeholder, value));
            }
            return expanded;
        });
    }

    @NotNull
    static String expandReplacement(@NotNull String initial, @NotNull UnaryOperator<String> expansionPass) {
        Set<String> seen = new HashSet<>();
        String text = initial;
        for (int pass = 0; pass < MAX_REPLACEMENT_PASSES && seen.add(text); pass++) {
            String expanded = expansionPass.apply(text);
            if (expanded.equals(text)) {
                return text;
            }
            text = expanded;
        }
        return text;
    }
}
