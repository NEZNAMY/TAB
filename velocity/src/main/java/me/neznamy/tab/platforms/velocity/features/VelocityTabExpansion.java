package me.neznamy.tab.platforms.velocity.features;

import com.velocitypowered.api.proxy.Player;
import io.github.miniplaceholders.api.Expansion;
import lombok.Getter;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.platforms.velocity.hook.MiniPlaceholdersHook;
import me.neznamy.tab.shared.ProjectVariables;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TAB's expansion for MiniPlaceholders.
 */
@Getter
public class VelocityTabExpansion implements TabExpansion {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    /** List of all placeholders offered by the plugin for command suggestions */
    @NotNull
    private final List<String> placeholders = Collections.unmodifiableList(Arrays.asList(
            "%tab_tabprefix%",
            "%tab_tabsuffix%",
            "%tab_tagprefix%",
            "%tab_tagsuffix%",
            "%tab_customtabname%",
            "%tab_tabprefix_raw%",
            "%tab_tabsuffix_raw%",
            "%tab_tagprefix_raw%",
            "%tab_tagsuffix_raw%",
            "%tab_customtabname_raw%",
            "%tab_scoreboard_name%",
            "%tab_scoreboard_visible%",
            "%tab_bossbar_visible%",
            "%tab_nametag_visibility%",
            "%tab_replace_<placeholder>%",
            "%tab_placeholder_<placeholder>%"
    ));

    /** Registered expansion instance */
    @NotNull
    private final Expansion expansion;

    /**
     * Constructs new instance and registers placeholders into MiniPlaceholders.
     */
    public VelocityTabExpansion() {
        Expansion.Builder builder = Expansion.builder(ProjectVariables.PLUGIN_ID)
                .version(ProjectVariables.PLUGIN_VERSION)
                .author(ProjectVariables.PLUGIN_AUTHOR);

        List<String> internalPlaceholders = Arrays.asList(
                "tabprefix",
                "tabsuffix",
                "tagprefix",
                "tagsuffix",
                "customtabname",
                "tabprefix_raw",
                "tabsuffix_raw",
                "tagprefix_raw",
                "tagsuffix_raw",
                "customtabname_raw",
                "scoreboard_name",
                "scoreboard_visible",
                "bossbar_visible",
                "nametag_visibility"
        );
        for (String placeholder : internalPlaceholders) {
            builder.audiencePlaceholder(Player.class, placeholder, (player, queue, ctx) ->
                    createTag(getExpansionValue(player, placeholder))
            );
        }

        builder.audiencePlaceholder(Player.class, "replace", (player, queue, ctx) -> {
            if (!queue.hasNext()) {
                return Tag.selfClosingInserting(Component.text("<No placeholder>"));
            }
            String text = "%" + queue.pop().value() + "%";
            String textBefore;
            do {
                textBefore = text;
                for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(text)) {
                    text = text.replace(placeholder, TAB.getInstance().getPlaceholderManager().findReplacement(
                            placeholder,
                            MiniPlaceholdersHook.parseText(placeholder, player)
                    ));
                }
            } while (!textBefore.equals(text));
            return createTag(text);
        });

        builder.audiencePlaceholder(Player.class, "placeholder", (player, queue, ctx) -> {
            if (!queue.hasNext()) {
                return Tag.selfClosingInserting(Component.text("<No placeholder>"));
            }
            String requestedPlaceholder = "%" + queue.pop().value() + "%";
            PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
            pm.addUsedPlaceholder(requestedPlaceholder, pm);
            TabPlayer tabPlayer = TAB.getInstance().getPlayer(player.getUniqueId());
            if (tabPlayer == null) {
                return Tag.selfClosingInserting(Component.text("<Player is not loaded>"));
            }
            return createTag(pm.getPlaceholder(requestedPlaceholder).parse(tabPlayer));
        });

        builder.relationalPlaceholder("replace", (viewer, target, queue, ctx) -> {
            if (!queue.hasNext()) {
                return Tag.selfClosingInserting(Component.text("<No placeholder>"));
            }
            if (!(viewer instanceof Player) || !(target instanceof Player)) {
                return Tag.selfClosingInserting(Component.text("<Players must be online>"));
            }
            String text = "%" + queue.pop().value() + "%";
            String textBefore;
            do {
                textBefore = text;
                for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(text)) {
                    text = text.replace(placeholder, TAB.getInstance().getPlaceholderManager().findReplacement(
                            placeholder,
                            MiniPlaceholdersHook.parseRelational(placeholder, (Player) viewer, (Player) target)
                    ));
                }
            } while (!textBefore.equals(text));
            return createTag(text);
        });

        builder.relationalPlaceholder("placeholder", (viewer, target, queue, ctx) -> {
            if (!queue.hasNext()) {
                return Tag.selfClosingInserting(Component.text("<No placeholder>"));
            }
            if (!(viewer instanceof Player) || !(target instanceof Player)) {
                return Tag.selfClosingInserting(Component.text("<Players must be online>"));
            }
            String requestedPlaceholder = "%" + queue.pop().value() + "%";
            PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
            pm.addUsedPlaceholder(requestedPlaceholder, pm);
            Placeholder placeholder = pm.getPlaceholder(requestedPlaceholder);
            if (placeholder instanceof RelationalPlaceholderImpl rel) {
                TabPlayer v = TAB.getInstance().getPlayer(((Player) viewer).getUniqueId());
                TabPlayer t = TAB.getInstance().getPlayer(((Player) target).getUniqueId());
                if (v == null || t == null) {
                    return Tag.selfClosingInserting(Component.text("<Player is not loaded>"));
                }
                return createTag(rel.getLastValue(v, t));
            }
            return Tag.selfClosingInserting(Component.text("<Not a relational placeholder: " + requestedPlaceholder + ">"));
        });

        expansion = builder.build();
        expansion.register();
    }

    @NotNull
    private static Tag createTag(@NotNull String value) {
        return Tag.selfClosingInserting(LEGACY_SERIALIZER.deserialize(value));
    }

    @NotNull
    private static String getExpansionValue(@NotNull Player player, @NotNull String identifier) {
        TabPlayer tabPlayer = TAB.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer == null) return "<Player is not loaded>";
        String value = tabPlayer.expansionData.getValue(identifier);
        return value == null ? "" : value;
    }

    @Override
    public void unregisterExpansion() {
        expansion.unregister();
    }
}
