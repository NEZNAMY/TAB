package me.neznamy.tab.platforms.fabric.hook;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import lombok.Getter;
import me.neznamy.tab.shared.ProjectVariables;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * TAB's expansion for Text PlaceholderAPI
 */
@Getter
public class FabricTabExpansion implements TabExpansion {

    /**
     * Constructs new instance and registers internal placeholders.
     */
    public FabricTabExpansion() {
        List<String> placeholders = Arrays.asList(
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
        for (String placeholder : placeholders) {
            registerPlaceholder(placeholder, (ctx, arg) -> {
                if (!ctx.hasPlayer()) return PlaceholderResult.invalid("No player!");
                TabPlayer player = TAB.getInstance().getPlayer(ctx.player().getUUID());
                return PlaceholderResult.value(player.expansionValues.get(placeholder));
            });
        }

        registerPlaceholder("replace", (ctx, arg) -> {
            if (!ctx.hasPlayer()) return PlaceholderResult.invalid("No player!");
            if (arg == null) return PlaceholderResult.invalid("No placeholder!");

            String text = "%" + arg + "%";
            String textBefore;
            do {
                textBefore = text;
                for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(text)) {
                    text = text.replace(placeholder, TAB.getInstance().getPlaceholderManager().findReplacement(placeholder,
                            Placeholders.parseText(
                                    Component.literal(placeholder),
                                    PlaceholderContext.of(ctx.player())
                            ).getString()));
                }
            } while (!textBefore.equals(text));

            return PlaceholderResult.value(text);
        });

        registerPlaceholder("placeholder", (ctx, arg) -> {
            if (arg == null) return PlaceholderResult.invalid("No placeholder!");

            TabPlayer player = ctx.hasPlayer() ? TAB.getInstance().getPlayer(ctx.player().getUUID()) : null;

            String placeholder = "%"+arg+"%";
            PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
            manager.addUsedPlaceholder(placeholder, manager);
            return PlaceholderResult.value(manager.getPlaceholder(placeholder).getLastValue(player));
        });
    }

    private void registerPlaceholder(String identifier, PlaceholderHandler handler) {
        Placeholders.register(ResourceLocation.tryParse(ProjectVariables.PLUGIN_ID+":"+identifier), handler);
    }


    @Override
    public void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value) {
        player.expansionValues.put(key, value);
    }

    @Override
    public void unregisterExpansion() {}
}
