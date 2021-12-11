package me.neznamy.tab.shared.features;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.nametags.NameTag;

public class NickCompatibility extends TabFeature {

	private final HashMap<TabPlayer, String> nickedPlayers = new HashMap<>();
	private final NameTag nameTags = (NameTag) TAB.getInstance().getTeamManager();
	private final BelowName belowname = (BelowName) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BELOW_NAME);
	private final YellowNumber yellownumber = (YellowNumber) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.YELLOW_NUMBER);
	
	public NickCompatibility() {
		super("Nick compatibility", null);
		TAB.getInstance().debug("Loaded NickCompatibility feature");
	}
	
	@Override
	public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo packet) {
		if (packet.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData data : packet.getEntries()) {
			TabPlayer packetPlayer = TAB.getInstance().getPlayerByTablistUUID(data.getUniqueId());
			if (packetPlayer == null || packetPlayer == receiver) continue;
			if (!packetPlayer.getName().equals(data.getName())) {
				if (!nickedPlayers.containsKey(packetPlayer)) {
					nickedPlayers.put(packetPlayer, data.getName());
					TAB.getInstance().debug("Processing name change of player " + packetPlayer.getName() + " to " + data.getName());
					processNameChange(packetPlayer, data.getName());
				}
			} else if (nickedPlayers.containsKey(packetPlayer)) {
				nickedPlayers.remove(packetPlayer);
				TAB.getInstance().debug("Processing name restore of player " + packetPlayer.getName());
				processNameChange(packetPlayer, data.getName());
			}
		}
	}
	
	private void processNameChange(TabPlayer player, String name) {
		TAB.getInstance().getCPUManager().runMeasuredTask("processing nickname change", this, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, () -> {
			
			if (nameTags != null && !nameTags.hasTeamHandlingPaused(player)) {
				for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
					viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(player.getTeamName()), this);
					String replacedPrefix = player.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
					String replacedSuffix = player.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer);
					viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(player.getTeamName(), replacedPrefix, replacedSuffix, nameTags.translate(nameTags.getTeamVisibility(player, viewer)),
							nameTags.translate(nameTags.getCollisionManager().getCollision(player)), Collections.singletonList(name), 0), this);
				}
			}
			if (belowname != null) {
				int value = belowname.getValue(player);
				for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
					if (all.getWorld().equals(player.getWorld()) && Objects.equals(all.getServer(), player.getServer()))
						all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, BelowName.OBJECTIVE_NAME, getNickname(player), value), this);
				}
			}
			if (yellownumber != null) {
				int value = yellownumber.getValue(player);
				for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
					all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, YellowNumber.OBJECTIVE_NAME, getNickname(player), value), this);
				}
			}
		});
	}
	
	public String getNickname(TabPlayer player) {
		return nickedPlayers.getOrDefault(player, player.getName());
	}
}