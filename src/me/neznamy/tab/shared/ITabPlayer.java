package me.neznamy.tab.shared;

import java.util.*;
import java.util.Map.Entry;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.ArmorStand;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagLineManager;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.Scoreboard;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.BossBar;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.features.BossBar.BossBarLine;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public abstract class ITabPlayer {

	public String name;
	public UUID uniqueId;
	public UUID tablistId;
	public String world;
	private String permissionGroup = "null";
	public String teamName;
	private String rank = "&7No Rank";

	public Map<String, Property> properties = new HashMap<String, Property>();
	private long lastRefreshGroup;
	public List<ArmorStand> armorStands = new ArrayList<ArmorStand>();
	public ProtocolVersion version = ProtocolVersion.SERVER_VERSION;
	public Object channel;
	public boolean nameTagVisible = true;
	public boolean bossbarVisible;
	private PlayerInfoData infoData;

	public boolean disabledHeaderFooter;
	public boolean disabledTablistNames;
	public boolean disabledNametag;
	public boolean disabledTablistObjective;
	public boolean disabledBossbar;
	public boolean disabledBelowname;

	private Scoreboard activeScoreboard;
	public boolean hiddenScoreboard;
	public boolean previewingNametag;
	public List<BossBarLine> activeBossBars = new ArrayList<BossBarLine>();
	public boolean lastCollision;
	public boolean lastVisibility;

	public void init() {
		updateGroupIfNeeded(false);
		updateAll();
		teamName = buildTeamName();
		updateDisabledWorlds(getWorldName());
		if (Shared.features.containsKey("playerlist")) infoData = new PlayerInfoData(name, tablistId, null, 0, EnumGamemode.CREATIVE, name);
	}

	//bukkit only
	public void setTeamVisible(boolean p0) {
	}

	public boolean hasInvisibility() {
		return false;
	}

	//per-type
	public abstract String getGroupFromPermPlugin();

	public abstract String[] getGroupsFromPermPlugin();

	public abstract boolean hasPermission(String permission);

	public abstract long getPing();

	public abstract void sendPacket(Object nmsPacket);

	public abstract void sendMessage(String message);

	public abstract Object getSkin();
	
	public boolean getTeamPush() {
		return Configs.collision;
	}

	public String getName() {
		return name;
	}

	public UUID getUniqueId() {
		return uniqueId;
	}

	public UUID getTablistId() {
		return tablistId;
	}

	public ProtocolVersion getVersion() {
		return version;
	}

	public Object getChannel() {
		return channel;
	}

	public String getTeamName() {
		return teamName;
	}

	public String getRank() {
		return rank;
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

	public List<BossBarLine> getActiveBossBars() {
		return activeBossBars;
	}

	public String getWorldName() {
		return world;
	}

	public List<ArmorStand> getArmorStands() {
		return armorStands;
	}

	public PlayerInfoData getInfoData() {
		return infoData;
	}

	public String setProperty(String identifier, String rawValue) {
		Property p = properties.get(identifier);
		if (p == null) {
			properties.put(identifier, new Property(this, rawValue));
		} else {
			p.changeRawValue(rawValue);
		}
		return rawValue;
	}

	public boolean isListNameUpdateNeeded() {
		getGroup();
		boolean tabprefix = properties.get("tabprefix").isUpdateNeeded();
		boolean customtabname = properties.get("customtabname").isUpdateNeeded();
		boolean tabsuffix = properties.get("tabsuffix").isUpdateNeeded();
		return (tabprefix || customtabname || tabsuffix);
	}

	public void updatePlayerListName() {
		isListNameUpdateNeeded(); //triggering updates to replaced values
		for (ITabPlayer all : Shared.getPlayers()) {
			all.sendPacket(PacketAPI.buildPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, getInfoData()), all.getVersion()));
		}
	}

	public String getTabFormat(ITabPlayer other) {
		Property prefix = properties.get("tabprefix");
		Property name = properties.get("customtabname");
		Property suffix = properties.get("tabsuffix");
		String format;
		if (Premium.allignTabsuffix) {
			format = ((AlignedSuffix)Shared.features.get("alignedsuffix")).fixTextWidth(prefix.get() + name.get(), suffix.get());
		} else {
			format = prefix.get() + name.get() + suffix.get();
		}
		return (prefix.hasRelationalPlaceholders() || name.hasRelationalPlaceholders() || suffix.hasRelationalPlaceholders()) ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(this, other, format) : format;
	}

	public void updateTeam() {
		if (disabledNametag) return;
		String newName = buildTeamName();
		if (teamName.equals(newName)) {
			updateTeamData();
		} else {
			unregisterTeam();
			teamName = newName;
			registerTeam();
		}
		if (Shared.features.containsKey("nametagx")) {
			NameTagLineManager.refreshNames(this);
			fixArmorStandHeights();
		}
	}

	public void fixArmorStandHeights() {
		NameTagLineManager.refreshNames(this);
		double currentY = -Configs.SECRET_NTX_space;;
		for (ArmorStand as : getArmorStands()) {
			if (as.hasStaticOffset()) continue;
			if (as.property.get().length() != 0) {
				currentY += Configs.SECRET_NTX_space;
				as.setOffset(currentY);
			}
		}
	}
	
	private boolean getTeamVisibility() {
		if (TABAPI.hasHiddenNametag(getUniqueId()) || Configs.SECRET_invisible_nametags) return false;
		return !Shared.features.containsKey("nametagx") && nameTagVisible;
	}

	public String getGroup() {
		if (System.currentTimeMillis() - lastRefreshGroup > 1000L) {
			lastRefreshGroup = System.currentTimeMillis();
			updateGroupIfNeeded(true);
		}
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
		} else {
			if (Configs.usePrimaryGroup) {
				newGroup = getGroupFromPermPlugin();
			} else {
				String[] playerGroups = getGroupsFromPermPlugin();
				if (playerGroups != null && playerGroups.length > 0) {
					loop:
						for (Object entry : Configs.primaryGroupFindingList) {
							for (String playerGroup : playerGroups) {
								if (playerGroup == null) playerGroup = "null"; //ultrapermissions stuff
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
			if (newGroup == null) {
				//ultrapermissions
				Shared.errorManager.printError("New updated group of " + getName() + " is null?");
				newGroup = "null";
				return;
			}
			permissionGroup = newGroup;
			if (updateDataIfChanged) {
				updateAll();
				forceUpdateDisplay();
			}
		}
	}

	public void updateAll() {
		setProperty("tablist-objective", TabObjective.rawValue);
		setProperty("belowname-number", BelowName.number);
		setProperty("belowname-text", BelowName.text);
		setProperty("tabprefix", getValue("tabprefix"));
		setProperty("tagprefix", getValue("tagprefix"));
		setProperty("tabsuffix", getValue("tabsuffix"));
		setProperty("tagsuffix", getValue("tagsuffix"));
		String temp;
		setProperty("customtabname", (temp = getValue("customtabname")).length() == 0 ? getName() : temp);
		setProperty("customtagname", (temp = getValue("customtagname")).length() == 0 ? getName() : temp);
		setProperty("nametag", properties.get("tagprefix").getCurrentRawValue() + properties.get("customtagname").getCurrentRawValue() + properties.get("tagsuffix").getCurrentRawValue());
		for (String property : Premium.dynamicLines) {
			if (!property.equals("nametag")) setProperty(property, getValue(property));
		}
		for (String property : Premium.staticLines.keySet()) {
			if (!property.equals("nametag")) setProperty(property, getValue(property));
		}
		rank = String.valueOf(Configs.rankAliases.get("_OTHER_")+""); //it is a string, but some geniuses might like something else..
		for (Entry<String, Object> entry : Configs.rankAliases.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(permissionGroup)) {
				rank = (String) entry.getValue();
				break;
			}
		}
		if (rank == null) rank = permissionGroup;
		updateRawHeaderAndFooter();
	}

	private String getValue(Object property) {
		String playerGroupFromConfig = permissionGroup.replace(".", "@#@");
		String worldGroup = getWorldGroupOf(getWorldName());
		String value;
		if ((value = Configs.config.getString("per-" + Shared.separatorType + "-settings." + worldGroup + ".Users." + getName() + "." + property)) != null)
			return value;
		if ((value = Configs.config.getString("per-" + Shared.separatorType + "-settings." + worldGroup + ".Users." + getUniqueId().toString() + "." + property)) != null)
			return value;
		if ((value = Configs.config.getString("Users." + getName() + "." + property)) != null) return value;
		if ((value = Configs.config.getString("Users." + getUniqueId().toString() + "." + property)) != null) return value;
		if ((value = Configs.config.getString("per-" + Shared.separatorType + "-settings." + worldGroup + ".Groups." + playerGroupFromConfig + "." + property)) != null)
			return value;
		if ((value = Configs.config.getString("per-" + Shared.separatorType + "-settings." + worldGroup + ".Groups._OTHER_." + property)) != null)
			return value;
		if ((value = Configs.config.getString("Groups." + playerGroupFromConfig + "." + property)) != null) return value;
		if ((value = Configs.config.getString("Groups._OTHER_." + property)) != null) return value;
		return "";
	}

	private void updateRawHeaderAndFooter() {
		updateRawValue("header");
		updateRawValue("footer");
	}
	
	private void updateRawValue(String name) {
		String worldGroup = getWorldGroupOf(getWorldName());
		StringBuilder rawValue = new StringBuilder();
		List<String> lines = Configs.config.getStringList("per-" + Shared.separatorType + "-settings." + worldGroup + ".Users." + getName() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("per-" + Shared.separatorType + "-settings." + worldGroup + ".Users." + getUniqueId().toString() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("Users." + getName() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("Users." + getUniqueId().toString() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("per-" + Shared.separatorType + "-settings." + worldGroup + ".Groups." + permissionGroup + "." + name);
		if (lines == null) lines = Configs.config.getStringList("per-" + Shared.separatorType + "-settings." + worldGroup + "." + name);
		if (lines == null) lines = Configs.config.getStringList("Groups." + permissionGroup + "." + name);
		if (lines == null) lines = Configs.config.getStringList(name);
		if (lines == null) lines = new ArrayList<String>();
		int i = 0;
		for (String line : lines) {
			if (++i > 1) rawValue.append("\n" + Placeholders.colorChar + "r");
			rawValue.append(line);
		}
		setProperty(name, rawValue.toString());
	}

	@SuppressWarnings("unchecked")
	private String getWorldGroupOf(String world) {
		Object rawWorlds = Configs.config.get("per-" + Shared.separatorType + "-settings");
		if (!(rawWorlds instanceof Map)) return world;
		Map<String, Object> worlds = (Map<String, Object>) rawWorlds;
		if (worlds.isEmpty()) return world;
		for (String worldGroup : worlds.keySet()) {
			for (String localWorld : worldGroup.split(Configs.SECRET_multiWorldSeparator)) {
				if (localWorld.equalsIgnoreCase(world)) return worldGroup;
			}
		}
		return world;
	}

	public String buildTeamName() {
		if (Premium.is()) {
			return Premium.sortingType.getTeamName(this);
		}
		String name = null;
		if (Configs.sortByPermissions) {
			for (String permissionGroup : Configs.sortedGroups.keySet()) {
				if (hasPermission("tab.sort." + permissionGroup)) {
					name = Configs.sortedGroups.get(permissionGroup);
					break;
				}
			}
		} else {
			for (String permissionGroup : Configs.sortedGroups.keySet()) {
				if (permissionGroup.equalsIgnoreCase(this.permissionGroup)) {
					name = Configs.sortedGroups.get(permissionGroup);
					break;
				}
			}
		}
		if (name == null) {
			Shared.errorManager.oneTimeConsoleError("Group \"" + permissionGroup + "\" is not defined in sorting list! This will result in unwanted behavior.");
			if (Shared.features.containsKey("playerlist")) {
				name = properties.get("tabprefix").get();
			} else {
				name = properties.get("tagprefix").get();
			}
		}
		if (name == null || name.length() == 0) {
			name = "&f";
		} else {
			if (name.contains("%")) {
				for (Placeholder pl : Placeholders.getAllUsed()) {
					if (name.contains(pl.getIdentifier())) name = name.replace(pl.getIdentifier(), pl.getValue(this));
				}
			}
		}
		if (name.length() > 12) {
			name = name.substring(0, 12);
		}
		name += getName();
		if (name.length() > 15) {
			name = name.substring(0, 15);
		}
		main:
		for (int i = 65; i <= 255; i++) {
			String potentialTeamName = name + (char)i;
			for (ITabPlayer all : Shared.getPlayers()) {
				if (all == this) continue;
				if (all.getTeamName().equals(potentialTeamName)) {
					continue main;
				}
			}
			if (!Premium.caseSensitive) potentialTeamName = potentialTeamName.toLowerCase();
			return potentialTeamName;
		}
		return getName();
	}

	public void updateTeamData() {
		if (disabledNametag) return;
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		boolean tagprefixUpdate = tagprefix.isUpdateNeeded();
		boolean tagsuffixUpdate = tagsuffix.isUpdateNeeded();
		boolean collision = getTeamPush();
		boolean visible = getTeamVisibility();
		if (tagprefixUpdate || tagsuffixUpdate || lastCollision != collision || lastVisibility != visible) {
			String replacedPrefix = tagprefix.get();
			String replacedSuffix = tagsuffix.get();
			lastCollision = collision;
			lastVisibility = visible;
			for (ITabPlayer all : Shared.getPlayers()) {
				String currentPrefix = tagprefix.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(this, all, replacedPrefix) : replacedPrefix;
				String currentSuffix = tagsuffix.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(this, all, replacedSuffix) : replacedSuffix;
				PacketAPI.updateScoreboardTeamPrefixSuffix(all, teamName, currentPrefix, currentSuffix, visible, collision);
			}
		}
	}

	public void registerTeam() {
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		String replacedPrefix = tagprefix.get();
		String replacedSuffix = tagsuffix.get();
		for (ITabPlayer all : Shared.getPlayers()) {
			String currentPrefix = tagprefix.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(this, all, replacedPrefix) : replacedPrefix;
			String currentSuffix = tagsuffix.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(this, all, replacedSuffix) : replacedSuffix;
			PacketAPI.registerScoreboardTeam(all, teamName, currentPrefix, currentSuffix, lastVisibility = getTeamVisibility(), lastCollision = getTeamPush(), Arrays.asList(getName()));
		}
	}

	public void registerTeam(ITabPlayer to) {
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		String replacedPrefix = tagprefix.get();
		String replacedSuffix = tagsuffix.get();
		if (tagprefix.hasRelationalPlaceholders()) replacedPrefix = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(this, to, replacedPrefix);
		if (tagsuffix.hasRelationalPlaceholders()) replacedSuffix = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(this, to, replacedSuffix);
		PacketAPI.registerScoreboardTeam(to, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush(), Arrays.asList(getName()));
	}

	public void unregisterTeam() {
		for (ITabPlayer p : Shared.getPlayers()) {
			PacketAPI.unregisterScoreboardTeam(p, teamName);
		}
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
		updateDisabledWorlds(to);
		updateGroupIfNeeded(false);
		updateAll();
		Shared.features.values().forEach(f -> f.onWorldChange(this, from, to));
	}

	public void detectBossBarsAndSend() {
		BossBar feature = (BossBar) Shared.features.get("bossbar");
		activeBossBars.clear();
		if (disabledBossbar || !bossbarVisible) return;
		for (String defaultBar : feature.defaultBars) {
			BossBarLine bar = feature.getLine(defaultBar);
			if (bar != null && bar.hasPermission(this)) {
				PacketAPI.createBossBar(this, bar);
				activeBossBars.add(bar);
			}
		}
		for (String announcement : feature.announcements) {
			BossBarLine bar = feature.getLine(announcement);
			if (bar != null && bar.hasPermission(this)) {
				PacketAPI.createBossBar(this, bar);
			}
		}
		if (feature.perWorld.get(getWorldName()) != null)
			for (String worldbar : feature.perWorld.get(getWorldName())) {
				BossBarLine bar = feature.getLine(worldbar);
				if (bar != null && bar.hasPermission(this)) {
					PacketAPI.createBossBar(this, bar);
					activeBossBars.add(bar);
				}
			}
	}

	public void sendCustomPacket(UniversalPacketPlayOut packet) {
		try {
			sendPacket(PacketAPI.buildPacket(packet, version));
		} catch (Throwable e) {
			Shared.errorManager.printError("An error occurred when creating " + packet.getClass().getSimpleName(), e);
		}
	}
	public void sendCustomBukkitPacket(PacketPlayOut packet) {
		try {
			sendPacket(packet.toNMS(version));
		} catch (Throwable e) {
			Shared.errorManager.printError("An error occurred when creating " + packet.getClass().getSimpleName(), e);
		}
	}
	public void forceUpdateDisplay() {
		if (Shared.features.containsKey("playerlist") && !disabledTablistNames) updatePlayerListName();
		if ((Shared.features.containsKey("nametag16")) || Shared.features.containsKey("nametagx")) {
			if (!disabledNametag) {
				unregisterTeam();
				registerTeam();
			}
		}
		if (Shared.features.containsKey("nametagx") && !disabledNametag) ((TabPlayer)this).restartArmorStands();
	}
}