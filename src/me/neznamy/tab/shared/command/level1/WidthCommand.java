package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;

public class WidthCommand extends SubCommand{

	public WidthCommand() {
		super("width", "tab.width");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (sender == null) {
			sendMessage(sender, "&cThis command must be ran from the game");
			return;
		}
		if (args.length == 1) {
			char c = args[0].charAt(0);
			List<IChatBaseComponent> messages = new ArrayList<IChatBaseComponent>();
			messages.add(new IChatBaseComponent("§b[TAB] Click the line with closest width"));
			messages.add(new IChatBaseComponent("§b[TAB] §ki §e|§b (1 pixel) §7§l[Click to apply]").onClickRunCommand("/tab width " + c + " 1").onHoverShowText("Click to set width to 1 pixel"));
			messages.add(new IChatBaseComponent("§2[TAB] " + c + " §d|"));
			messages.add(new IChatBaseComponent("§b[TAB] §kl §e|§b (2 pixels) §7§l[Click to apply]").onClickRunCommand("/tab width " + c + " 2").onHoverShowText("Click to set width to 2 pixels"));
			messages.add(new IChatBaseComponent("§b[TAB] §kii §e|§b (3 pixels) §7§l[Click to apply]").onClickRunCommand("/tab width " + c + " 3").onHoverShowText("Click to set width to 3 pixels"));
			messages.add(new IChatBaseComponent("§2[TAB] " + c + " §d|"));
			messages.add(new IChatBaseComponent("§b[TAB] §kil §e|§b (4 pixels) §7§l[Click to apply]").onClickRunCommand("/tab width " + c + " 4").onHoverShowText("Click to set width to 4 pixels"));
			messages.add(new IChatBaseComponent("§b[TAB] §kll §e|§b (5 pixels) §7§l[Click to apply]").onClickRunCommand("/tab width " + c + " 5").onHoverShowText("Click to set width to 5 pixels"));
			messages.add(new IChatBaseComponent("§2[TAB] " + c + " §d|"));
			messages.add(new IChatBaseComponent("§b[TAB] §kiil §e|§b (6 pixels) §7§l[Click to apply]").onClickRunCommand("/tab width " + c + " 6").onHoverShowText("Click to set width to 6 pixels"));
			messages.add(new IChatBaseComponent("§b[TAB] §kill §e|§b (7 pixels) §7§l[Click to apply]").onClickRunCommand("/tab width " + c + " 7").onHoverShowText("Click to set width to 7 pixels"));
			messages.add(new IChatBaseComponent("§2[TAB] " + c + " §d|"));
			messages.add(new IChatBaseComponent("§b[TAB] §klll §e|§b (8 pixels) §7§l[Click to apply]").onClickRunCommand("/tab width " + c + " 8").onHoverShowText("Click to set width to 8 pixels"));
			for (IChatBaseComponent message : messages) {
				sender.sendCustomPacket(new PacketPlayOutChat(message));
			}
		} else if (args.length == 2) {
			char c = args[0].charAt(0);
			int width = Integer.parseInt(args[1]);
			if (!Premium.premiumconfig.hasConfigOption("extra-character-widths")) Premium.premiumconfig.set("extra-character-widths", new HashMap<Integer, Integer>());
			Premium.premiumconfig.getConfigurationSection("extra-character-widths").put((int)c, width);
			Premium.premiumconfig.save();
			sendMessage(sender, "&2[TAB] Successfully set width of &6" + c + " &2(&6" + (int)c + "&2) to &6" + width + "&2 pixels.");
		} else {
			sendMessage(sender, "Usage: /tab width <character>");
		}
	}
}