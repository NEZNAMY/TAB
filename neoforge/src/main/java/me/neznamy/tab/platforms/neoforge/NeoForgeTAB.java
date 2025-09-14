package me.neznamy.tab.platforms.neoforge;

import me.neznamy.tab.shared.TAB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for NeoForge TAB implementation.
 */
@Mod(value = "tab", dist = Dist.DEDICATED_SERVER)
public class NeoForgeTAB {

	/**
	 * Constructs new instance and registers necessary events.
	 */
	public NeoForgeTAB() {
		IEventBus EVENT_BUS = NeoForge.EVENT_BUS;
		EVENT_BUS.addListener((RegisterCommandsEvent event) -> new NeoForgeTabCommand().onRegisterCommands(event.getDispatcher()));
		EVENT_BUS.addListener((ServerStartingEvent event) -> TAB.create(new NeoForgePlatform(event.getServer())));
		EVENT_BUS.addListener((ServerStoppingEvent event) -> TAB.getInstance().unload());
	}

	/**
	 * Gets level name with dimension suffix to match Bukkit's behavior.
	 *
	 * @param   level
	 *          Level to get name of
	 * @return  Level name with dimension suffix
	 */
	@NotNull
	public static String getLevelName(@NotNull Level level) {
		String path = level.dimension().location().getPath();
		return ((ServerLevelData)level.getLevelData()).getLevelName() + switch (path) {
			case "overworld" -> ""; // No suffix for overworld
			case "the_nether" -> "_nether";
			default -> "_" + path; // End + default behavior for other dimensions created by mods
		};
	}
}
