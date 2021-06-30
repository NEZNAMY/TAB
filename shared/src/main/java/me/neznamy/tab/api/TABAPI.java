package me.neznamy.tab.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import me.neznamy.tab.shared.placeholders.conditions.simple.SimpleCondition;

/**
 * The primary API class to get instances of other API classes
 */
public class TABAPI {

	//placeholders registered via API
	private static final Map<String, Placeholder> apiPlaceholders = new HashMap<>();

	private TABAPI() {
	}

	/**
	 * Returns player object from given UUID
	 * @return player object from given UUID
	 * @param id - Player UUID
	 */
	public static TabPlayer getPlayer(UUID id) {
		return TAB.getInstance().getPlayer(id);
	}

	/**
	 * Returns player object from given name
	 * @return player object from given name
	 * @param name - Player name
	 */
	public static TabPlayer getPlayer(String name) {
		return TAB.getInstance().getPlayer(name);
	}

	/**
	 * Returns true if enabled, false if disabled
	 * @return Whether unlimited nametag mode is enabled or not
	 * @see enableUnlimitedNameTagModePermanently
	 */
	public static boolean isUnlimitedNameTagModeEnabled() {
		return TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx");
	}

	/**
	 * Enables unlimited nametag mode permanently in config
	 * @see isUnlimitedNameTagModeEnabled
	 */
	public static void enableUnlimitedNameTagModePermanently() {
		if (isUnlimitedNameTagModeEnabled()) return;
		TAB.getInstance().getConfiguration().getConfig().set("change-nametag-prefix-suffix", true);
		TAB.getInstance().getConfiguration().getConfig().set("unlimited-nametag-prefix-suffix-mode.enabled", true);
		TAB.getInstance().unload();
		TAB.getInstance().load();
	}

	/**
	 * Registers a player placeholder (placeholder with player-specific output)
	 * @param placeholder - Placeholder handler
	 * @see registerServerPlaceholder
	 * @see registerServerConstant
	 */
	public static void registerPlayerPlaceholder(PlayerPlaceholder placeholder) {
		Preconditions.checkNotNull(placeholder, "placeholder");
		apiPlaceholders.put(placeholder.getIdentifier(), placeholder);
		PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
		pl.registerPlaceholder(placeholder);
		pl.getAllUsedPlaceholderIdentifiers().add(placeholder.getIdentifier());
		pl.refreshPlaceholderUsage();
	}

	/**
	 * Registers a server placeholder (placeholder with same output for all players)
	 * @param placeholder - Placeholder handler
	 * @see registerPlayerPlaceholder
	 * @see registerServerConstant
	 */
	public static void registerServerPlaceholder(ServerPlaceholder placeholder) {
		Preconditions.checkNotNull(placeholder, "placeholder");
		apiPlaceholders.put(placeholder.getIdentifier(), placeholder);
		PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
		pl.registerPlaceholder(placeholder);
		pl.getAllUsedPlaceholderIdentifiers().add(placeholder.getIdentifier());
		pl.refreshPlaceholderUsage();
	}

	/**
	 * Registers a relational placeholder
	 * @param placeholder - Placeholder handler
	 */
	public static void registerRelationalPlaceholder(RelationalPlaceholder placeholder) {
		Preconditions.checkNotNull(placeholder, "placeholder");
		apiPlaceholders.put(placeholder.getIdentifier(), placeholder);
		PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
		pl.registerPlaceholder(placeholder);
		pl.getAllUsedPlaceholderIdentifiers().add(placeholder.getIdentifier());
		pl.refreshPlaceholderUsage();
	}

	/**
	 * Creates a new scoreboard
	 * @param name - name of the scoreboard
	 * @param title - the scoreboard title
	 * @param lines - up to 15 lines of text (supports placeholders)
	 * @return The new scoreboard
	 */
	public static Scoreboard createScoreboard(String name, String title, List<String> lines) {
		for (String line : lines) {
			TAB.getInstance().getPlaceholderManager().checkForRegistration(line);
		}
		ScoreboardManager sbm = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (sbm == null) throw new IllegalStateException("Scoreboard feature is not enabled");
		Scoreboard sb = new ScoreboardImpl(sbm, name, title, lines);
		sbm.getApiScoreboards().add(sb);
		return sb;
	}

	/**
	 * Registers a custom permission plugin
	 * @param permission - permission plugin provider
	 */
	public static void registerPermissionPlugin(PermissionPlugin permission) {
		TAB.getInstance().setPermissionPlugin(permission);
	}

	/**
	 * Creates bossbar with specified parameters
	 * @param name - internal name of bossbar
	 * @param title - title
	 * @param progress - progress (0-1)
	 * @param color - color
	 * @param style - style
	 * @return the bossbar
	 */
	public static BossBar createBossBar(String name, String title, float progress, BarColor color, BarStyle style) {
		return createBossBar(name, title, String.valueOf(progress), color.toString(), style.toString());
	}

	/**
	 * Creates bossbar with specified parameters as strings to allow placeholder support
	 * @param name - internal name of bossbar
	 * @param title - title
	 * @param progress - progress
	 * @param color - color
	 * @param style - style
	 * @return the bossbar
	 */
	public static BossBar createBossBar(String name, String title, String progress, String color, String style) {
		me.neznamy.tab.shared.features.bossbar.BossBar feature = (me.neznamy.tab.shared.features.bossbar.BossBar) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (feature == null) throw new IllegalStateException("Bossbar feature is not enabled");
		BossBar bar = new BossBarLine(name, null, color, style, title, progress);
		feature.getLines().put(bar.getName(), (BossBarLine) bar);
		return bar;
	}

	/**
	 * Returns placeholders registered via API
	 * @return placeholders registered via API
	 */
	public static Map<String, Placeholder> getAPIPlaceholders(){
		return apiPlaceholders;
	}

	/**
	 * Registers custom condition type and return true if it was registered or false if it was already registered. Useful since
	 * /tab reload does not remove registered condition types.
	 * @param identifier - identifier to detect if condition is of this type
	 * @param clazz - class that handles the condition
	 * @return true if it was registered, false if it already was registered before
	 */
	public static boolean registerSubCondition(String identifier, Class<? extends SimpleCondition> clazz) {
		return SimpleCondition.getConditionTypes().put(identifier, clazz) == null;
	}
}