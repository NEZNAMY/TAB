package me.neznamy.tab.shared;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Charsets;

import io.netty.channel.Channel;
import me.neznamy.tab.api.EnumProperty;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.ArmorStandManager;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.premium.Scoreboard;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.premium.SortingType;
import me.neznamy.tab.shared.command.level1.PlayerCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class ITabPlayer implements TabPlayer{

	public String name;
	public UUID uniqueId;
	public UUID offlineId;
	public UUID correctId; //for velocity, the correct UUID to use in tablist
	public String world;
	private String permissionGroup = "< Not Initialized Yet >";
	public String teamName;

	public Map<String, Property> properties = new HashMap<String, Property>();
	private ArmorStandManager armorStandManager;
	protected ProtocolVersion version = ProtocolVersion.SERVER_VERSION;
	public Channel channel;
	public boolean nameTagVisible = true;
	public boolean bossbarVisible;

	public boolean disabledHeaderFooter;
	public boolean disabledTablistNames;
	public boolean disabledNametag;
	public boolean disabledTablistObjective;
	public boolean disabledBossbar;
	public boolean disabledBelowname;

	public boolean previewingNametag;
	public List<BossBarLine> activeBossBars = new ArrayList<BossBarLine>();
	public boolean lastCollision;
	public boolean lastVisibility;
	public boolean onJoinFinished;
	public boolean hiddenNametag;
	
	private Scoreboard activeScoreboard;
	public boolean hiddenScoreboard;
	public Scoreboard forcedScoreboard;

	public void init() {
		updateDisabledWorlds(getWorldName());
		updateGroupIfNeeded(false);
		offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
		correctId = uniqueId; //initialization to avoid NPEs
	}

	//bukkit only
	public boolean hasInvisibility() {
		return false;
	}

	//per-type

	public abstract boolean hasPermission(String permission);

	public abstract long getPing();

	public abstract void sendPacket(Object nmsPacket);

	public abstract void sendMessage(String message);

	public abstract void sendRawMessage(String message);

	public abstract Object getSkin();
	
	public boolean isVanished() {
		return false;
	}

	public org.bukkit.entity.Player getBukkitEntity() {
		throw new IllegalStateException("Wrong platform");
	}

	public ProxiedPlayer getBungeeEntity() {
		throw new IllegalStateException("Wrong platform");
	}

	public com.velocitypowered.api.proxy.Player getVelocityEntity() {
		throw new IllegalStateException("Wrong platform");
	}

	public boolean getTeamPush() {
		return Configs.getCollisionRule(world);
	}

	public String getName() {
		return name;
	}

	public UUID getUniqueId() {
		return uniqueId;
	}

	public UUID getOfflineId() {
		return offlineId;
	}

	public ProtocolVersion getVersion() {
		return version;
	}

	public boolean isStaff() {
		return hasPermission("tab.staff");
	}

	public void setActiveScoreboard(Scoreboard board) {
		activeScoreboard = board;
	}

	public Scoreboard getActiveScoreboard() {
		return activeScoreboard;
	}

	public String getWorldName() {
		return world;
	}

	public PlayerInfoData getInfoData() {
		return new PlayerInfoData(name, correctId, null, 0, EnumGamemode.CREATIVE, null);
	}
	
	public String getGroupFromPermPlugin() {
		try {
			return Shared.permissionPlugin.getPrimaryGroup(this);
		} catch (Throwable e) {
			return Shared.errorManager.printError("null", "Failed to get permission group of " + getName() + " using " + Shared.permissionPlugin.getName(), e);
		}
	}

	public String[] getGroupsFromPermPlugin() {
		try {
			return Shared.permissionPlugin.getAllGroups(this);
		} catch (Throwable e) {
			return Shared.errorManager.printError(new String[] {"null"}, "Failed to get permission groups of " + getName() + " using " + Shared.permissionPlugin.getName(), e);
		}
	}

	public String setProperty(String identifier, String rawValue, String source) {
		Property p = properties.get(identifier);
		if (p == null) {
			properties.put(identifier, new Property(this, rawValue, source));
		} else {
			p.changeRawValue(rawValue);
			p.setSource(source);
		}
		return rawValue;
	}

	public void updateTeam() {
		if (disabledNametag) return;
		if (teamName == null) return; //player not loaded yet
		String newName = SortingType.INSTANCE.getTeamName(this);
		if (teamName.equals(newName)) {
			updateTeamData();
		} else {
			unregisterTeam();
			teamName = newName;
			registerTeam();
		}
	}

	private boolean getTeamVisibility() {
		if (hiddenNametag || Configs.SECRET_invisible_nametags) return false;
		return !Shared.features.containsKey("nametagx") && nameTagVisible;
	}

	public String getGroup() {
		return permissionGroup;
	}

	public void updateGroupIfNeeded(boolean updateDataIfChanged) {
		String newGroup = "null";
		if (Configs.groupsByPermissions) {
			for (Object group : Configs.primaryGroupFindingList) {
				if (hasPermission("tab.group." + group)) {
					newGroup = String.valueOf(group);
					break;
				}
			}
			if (newGroup.equals("null")) {
				Shared.errorManager.oneTimeConsoleError("Player " + name + " does not have any group permission while assign-groups-by-permissions is enabled! Did you forget to add his group to primary-group-finding-list?");
			}
		} else {
			if (Configs.usePrimaryGroup) {
				newGroup = getGroupFromPermPlugin();
			} else {
				String[] playerGroups = getGroupsFromPermPlugin();
				if (playerGroups != null && playerGroups.length > 0) {
					loop:
						for (Object entry : Configs.primaryGroupFindingList) {
							for (String playerGroup : playerGroups) {
								if (playerGroup.equalsIgnoreCase(entry + "")) {
									newGroup = playerGroup;
									break loop;
								}
							}
						}
				if (playerGroups[0] != null && newGroup.equals("null")) newGroup = playerGroups[0];
				}
			}
		}
		if (!permissionGroup.equals(newGroup)) {
			permissionGroup = newGroup;
			if (updateDataIfChanged) {
				forceRefresh();
			}
		}
	}

	public void updateProperty(String property) {
		updateProperty(property, "");
	}
	public void updateProperty(String property, String ifnull) {
		String playerGroupFromConfig = permissionGroup.replace(".", "@#@");
		String worldGroup = getWorldGroupOf(getWorldName());
		String value;
		if ((value = Configs.config.getString("per-" + Shared.platform.getSeparatorType() + "-settings." + worldGroup + ".Users." + getName() + "." + property)) != null) {
			setProperty(property, value, "Player: " + getName() + ", " + Shared.platform.getSeparatorType() + ": " + worldGroup);
			return;
		}
		if ((value = Configs.config.getString("per-" + Shared.platform.getSeparatorType() + "-settings." + worldGroup + ".Users." + getUniqueId().toString() + "." + property)) != null) {
			setProperty(property, value, "PlayerUUID: " + getName() + ", " + Shared.platform.getSeparatorType() + ": " + worldGroup);
			return;
		}
		if ((value = Configs.config.getString("Users." + getName() + "." + property)) != null) {
			setProperty(property, value, "Player: " + getName());
			return;
		}
		if ((value = Configs.config.getString("Users." + getUniqueId().toString() + "." + property)) != null) {
			setProperty(property, value, "PlayerUUID: " + getName());
			return;
		}
		if ((value = Configs.config.getString("per-" + Shared.platform.getSeparatorType() + "-settings." + worldGroup + ".Groups." + playerGroupFromConfig + "." + property)) != null) {
			setProperty(property, value, "Group: " + permissionGroup + ", " + Shared.platform.getSeparatorType() + ": " + worldGroup);
			return;
		}
		if ((value = Configs.config.getString("per-" + Shared.platform.getSeparatorType() + "-settings." + worldGroup + ".Groups._OTHER_." + property)) != null) {
			setProperty(property, value, "Group: _OTHER_," + Shared.platform.getSeparatorType() + ": " + worldGroup);
			return;
		}
		if ((value = Configs.config.getString("Groups." + playerGroupFromConfig + "." + property)) != null) {
			setProperty(property, value, "Group: " + permissionGroup);
			return;
		}
		if ((value = Configs.config.getString("Groups._OTHER_." + property)) != null) {
			setProperty(property, value, "Group: _OTHER_");
			return;
		}
		setProperty(property, ifnull, "None");
	}

	

	@SuppressWarnings("unchecked")
	public String getWorldGroupOf(String world) {
		Map<String, Object> worlds = Configs.config.getConfigurationSection("per-" + Shared.platform.getSeparatorType() + "-settings");
		if (worlds.isEmpty()) return world;
		for (String worldGroup : worlds.keySet()) {
			for (String localWorld : worldGroup.split(Configs.SECRET_multiWorldSeparator)) {
				if (localWorld.equalsIgnoreCase(world)) return worldGroup;
			}
		}
		return world;
	}

	public void updateTeamData() {
		if (disabledNametag) return;
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		boolean collision = lastCollision = getTeamPush();
		boolean visible = getTeamVisibility();
		for (ITabPlayer viewer : Shared.getPlayers()) {
			String currentPrefix = tagprefix.getFormat(viewer);
			String currentSuffix = tagsuffix.getFormat(viewer);
			PacketAPI.updateScoreboardTeamPrefixSuffix(viewer, teamName, currentPrefix, currentSuffix, visible, collision);
		}
	}

	public void registerTeam() {
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		for (ITabPlayer viewer : Shared.getPlayers()) {
			String currentPrefix = tagprefix.getFormat(viewer);
			String currentSuffix = tagsuffix.getFormat(viewer);
			PacketAPI.registerScoreboardTeam(viewer, teamName, currentPrefix, currentSuffix, lastVisibility = getTeamVisibility(), lastCollision = getTeamPush(), Arrays.asList(getName()), null);
		}
	}

	public void registerTeam(ITabPlayer viewer) {
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		String replacedPrefix = tagprefix.getFormat(viewer);
		String replacedSuffix = tagsuffix.getFormat(viewer);
		PacketAPI.registerScoreboardTeam(viewer, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush(), Arrays.asList(getName()), null);
	}

	public void unregisterTeam() {
		if (teamName == null) return;
		Object packet = PacketPlayOutScoreboardTeam.REMOVE_TEAM(teamName).setTeamOptions(69).build(ProtocolVersion.SERVER_VERSION);
		for (ITabPlayer viewer : Shared.getPlayers()) {
			viewer.sendPacket(packet);
		}
	}

	public void unregisterTeam(ITabPlayer viewer) {
		viewer.sendCustomPacket(PacketPlayOutScoreboardTeam.REMOVE_TEAM(teamName).setTeamOptions(69));
	}

	private void updateDisabledWorlds(String world) {
		disabledHeaderFooter = isDisabledWorld(Configs.disabledHeaderFooter, world);
		disabledTablistNames = isDisabledWorld(Configs.disabledTablistNames, world);
		disabledNametag = isDisabledWorld(Configs.disabledNametag, world);
		disabledTablistObjective = isDisabledWorld(Configs.disabledTablistObjective, world);
		disabledBossbar = isDisabledWorld(Configs.disabledBossbar, world);
		disabledBelowname = isDisabledWorld(Configs.disabledBelowname, world);
	}
	public boolean isDisabledWorld(List<String> disabledWorlds, String world) {
		if (disabledWorlds == null) return false;
		if (disabledWorlds.contains("WHITELIST")) {
			for (String enabled : disabledWorlds) {
				if (enabled != null && enabled.equals(world)) return false;
			}
			return true;
		} else {
			for (String disabled : disabledWorlds) {
				if (disabled != null && disabled.equals(world)) return true;
			}
			return false;
		}
	}

	public void onWorldChange(String from, String to) {
		ITabPlayer player = this;
		Shared.featureCpu.runMeasuredTask("processing world change", CPUFeature.WORLD_SWITCH, new Runnable() {

			@Override
			public void run() {
				updateDisabledWorlds(to);
				updateGroupIfNeeded(false);
				Shared.worldChangeListeners.forEach(f -> f.onWorldChange(player, from, to));
			}
		});
	}

	public void sendCustomPacket(UniversalPacketPlayOut packet) {
		sendPacket(packet.build(getVersion()));
	}
	public void sendCustomBukkitPacket(PacketPlayOut packet) {
		try {
			sendPacket(packet.toNMS(getVersion()));
		} catch (InvocationTargetException e) {
			Shared.errorManager.printError("An error occurred when creating " + packet.getClass().getSimpleName(), e.getTargetException());
		} catch (Throwable e) {
			Shared.errorManager.printError("An error occurred when creating " + packet.getClass().getSimpleName(), e);
		}
	}
	
	
	/*
	 *  Implementing interface
	 */
	
	public void setValueTemporarily(EnumProperty type, String value) {
		Placeholders.checkForRegistration(value);
		properties.get(type.toString()).setTemporaryValue(value);
		if (Shared.features.containsKey("nametagx") && type.toString().contains("tag")) {
			setProperty("nametag",properties.get("tagprefix").getCurrentRawValue() + properties.get("customtagname").getCurrentRawValue() + properties.get("tagsuffix").getCurrentRawValue(), null);
		}
		forceRefresh();
	}
	public void setValuePermanently(EnumProperty type, String value) {
		Placeholders.checkForRegistration(value);
		properties.get(type.toString()).changeRawValue(value);
		((PlayerCommand)Shared.command.subcommands.get("player")).savePlayer(null, getName(), type.toString(), value);
		if (Shared.features.containsKey("nametagx") && type.toString().contains("tag")) {
			setProperty("nametag", properties.get("tagprefix").getCurrentRawValue() + properties.get("customtagname").getCurrentRawValue() + properties.get("tagsuffix").getCurrentRawValue(), null);
		}
		forceRefresh();
	}
	public String getTemporaryValue(EnumProperty type) {
		return properties.get(type.toString()).getTemporaryValue();
	}
	public boolean hasTemporaryValue(EnumProperty type) {
		return getTemporaryValue(type) != null;
	}
	public void removeTemporaryValue(EnumProperty type) {
		setValueTemporarily(type, null);
	}
	public String getOriginalValue(EnumProperty type) {
		return properties.get(type.toString()).getOriginalRawValue();
	}
	public void sendHeaderFooter(String header, String footer) {
		sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(header, footer));
	}
	public void hideNametag() {
		hiddenNametag = true;
		updateTeamData();
	}
	public void showNametag() {
		hiddenNametag = false;
		updateTeamData();
	}
	public boolean hasHiddenNametag() {
		return hiddenNametag;
	}
	public void forceRefresh() {
		Shared.refreshables.forEach(r -> r.refresh(this, true));
	}
	public void showScoreboard(me.neznamy.tab.api.Scoreboard scoreboard) {
		if (forcedScoreboard != null) {
			forcedScoreboard.unregister(this);
		}
		if (activeScoreboard != null) {
			activeScoreboard.unregister(this);
			activeScoreboard = null;
		}
		forcedScoreboard = (Scoreboard) scoreboard;
		((Scoreboard)scoreboard).register(this);
	}
	public void showScoreboard(String name) {
		ScoreboardManager sbm = ((ScoreboardManager) Shared.features.get("scoreboard"));
		if (sbm == null) throw new IllegalStateException("Scoreboard feature is not enabled");
		Scoreboard scoreboard = sbm.getScoreboards().get(name);
		if (scoreboard == null) throw new IllegalArgumentException("No scoreboard found with name: " + name);
		showScoreboard(scoreboard);
	}
	public void removeCustomScoreboard() {
		if (forcedScoreboard == null) return;
		ScoreboardManager sbm = ((ScoreboardManager) Shared.features.get("scoreboard"));
		if (sbm == null) throw new IllegalStateException("Scoreboard feature is not enabled");
		Scoreboard sb = sbm.getScoreboards().get(sbm.getHighestScoreboard(this));
		activeScoreboard = sb;
		sb.register(this);
		forcedScoreboard = null;
	}

	public ArmorStandManager getArmorStandManager() {
		return armorStandManager;
	}

	public void setArmorStandManager(ArmorStandManager armorStandManager) {
		this.armorStandManager = armorStandManager;
	}
}