package me.neznamy.tab.platforms.forge;

import me.neznamy.tab.shared.TAB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for Forge TAB implementation.
 */
@OnlyIn(Dist.DEDICATED_SERVER)
@Mod("tab")
public class ForgeTAB {

	/**
	 * Constructs new instance and registers necessary events.
	 */
	public ForgeTAB() {
		RegisterCommandsEvent.BUS.addListener(event -> new ForgeTabCommand().onRegisterCommands(event.getDispatcher()));
		ServerStartingEvent.BUS.addListener(event -> TAB.create(new ForgePlatform(event.getServer())));
		ServerStoppingEvent.BUS.addListener(event -> TAB.getInstance().unload());
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
