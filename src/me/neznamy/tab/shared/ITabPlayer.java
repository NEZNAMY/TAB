package me.neznamy.tab.shared;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.ArmorStand;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagLineManager;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.Scoreboard;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.BossBar.BossBarLine;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public abstract class ITabPlayer{

	public String name;
	public UUID uniqueId;
	public UUID tablistId;
	public String world;
	private String permissionGroup;
	public String teamName;
	private String rank = "§7No Rank";
	
	public HashMap<String, Property> properties = new HashMap<String, Property>();
	private long lastRefreshGroup;
	public List<ArmorStand> armorStands = new ArrayList<ArmorStand>();
	public ProtocolVersion version = ProtocolVersion.SERVER_VERSION; //preventing errors
	public Object channel;
	public boolean nameTagVisible = true;
	public boolean bossbarVisible = true;

	public boolean disabledHeaderFooter;
	public boolean disabledTablistNames;
	public boolean disabledNametag;
	public boolean disabledTablistObjective;
	public boolean disabledBossbar;

	private Scoreboard activeScoreboard;
	public boolean hiddenScoreboard;
	public boolean previewingNametag;
	public List<BossBarLine> activeBossBars = new ArrayList<BossBarLine>();
	public boolean lastCollision;

	public void init(String name, UUID uniqueId) {
		this.name = name;
		this.uniqueId = uniqueId;
		updateGroupIfNeeded();
		updateAll();
		if (NameTag16.enable || Configs.unlimitedTags) teamName = buildTeamName();
		disabledHeaderFooter = Configs.disabledHeaderFooter.contains(getWorldName());
		disabledTablistNames = Configs.disabledTablistNames.contains(getWorldName());
		disabledNametag = Configs.disabledNametag.contains(getWorldName());
		disabledTablistObjective = Configs.disabledTablistObjective.contains(getWorldName());
		disabledBossbar = Configs.disabledBossbar.contains(getWorldName());
	}

	//bukkit only
	public String getNickname() {return getName();}
	public String getMoney() {return "-";}
	public void setTeamVisible(boolean p0) {}
	public void restartArmorStands() {}
	public Integer getEntityId() {return 0;}
	public int getHealth() {return 0;}
	public boolean hasInvisibility() {return false;}

	//per-type
	public abstract void setPlayerListName();
	public abstract String getGroupFromPermPlugin();
	public abstract String[] getGroupsFromPermPlugin();
	public abstract boolean hasPermission(String permission);
	public abstract long getPing();
	public abstract void sendPacket(Object nmsPacket);
	public abstract void sendMessage(String message);
	public abstract boolean getTeamPush();

	public String getName() {
		return name;
	}
	public UUID getUniqueId() {
		return uniqueId;
	}
	public UUID getTablistId(){
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
	public List<BossBarLine> getActiveBossBars(){
		return activeBossBars;
	}
	public String getWorldName() {
		return world;
	}
	public List<ArmorStand> getArmorStands() {
		return armorStands;
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
	public void updatePlayerListName(boolean force) {
		getGroup();
		boolean tabprefix = properties.get("tabprefix").isUpdateNeeded();
		boolean customtabname = properties.get("customtabname").isUpdateNeeded();
		boolean tabsuffix = properties.get("tabsuffix").isUpdateNeeded();
		if (tabprefix || customtabname || tabsuffix || force) {
			setPlayerListName();
		}
	}
	public String getTabFormat(ITabPlayer other) {
		return Placeholders.setRelational(this, other, properties.get("tabprefix").get() + properties.get("customtabname").get() + properties.get("tabsuffix").get());
	}
	public void updateTeam() {
		if (disabledNametag) return;
		String newName = buildTeamName();
		if (teamName.equals(newName)) {
			updateTeamPrefixSuffix();
		} else {
			unregisterTeam();
			teamName = newName;
			registerTeam();
		}
		if (Configs.unlimitedTags) {
			for (ArmorStand as : armorStands) {
				as.setNameFormat(properties.get(as.getID()).getCurrentRawValue());
			}
			NameTagLineManager.refreshNames(this);
		}
	}
	private boolean getTeamVisibility() {
		if (TABAPI.hasHiddenNametag(getUniqueId())) return false;
		return !Configs.unlimitedTags && nameTagVisible;
	}
	public String getGroup() {
		if (System.currentTimeMillis() - lastRefreshGroup > 1000L) {
			lastRefreshGroup = System.currentTimeMillis();
			updateGroupIfNeeded();
		}
		return permissionGroup;
	}
	public void updateGroupIfNeeded() {
		String newGroup = null;
		if (Configs.usePrimaryGroup) {
			newGroup = getGroupFromPermPlugin();
		} else {
			String[] playerGroups = getGroupsFromPermPlugin();
			if (playerGroups != null && playerGroups.length > 0) {
				loop:
					for (Object entry : Configs.primaryGroupFindingList) {
						for (String playerGroup : playerGroups) {
							if (playerGroup.equalsIgnoreCase(entry+"")) {
								newGroup = playerGroup;
								break loop;
							}
						}
					}
				if (newGroup == null) newGroup = playerGroups[0];
			}
		}
		if (newGroup != null && (permissionGroup == null || !permissionGroup.equals(newGroup))) {
			permissionGroup = newGroup;
			updateAll();
		}
	}
	public void updateAll() {
		setProperty("tablist-objective", TabObjective.rawValue);
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
		rank = (String) Configs.rankAliases.get("_OTHER_");
		for (Entry<String, Object> entry : Configs.rankAliases.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(permissionGroup)) {
				rank = (String) entry.getValue();
				break;
			}
		}
		updateRawHeaderAndFooter();
	}
	private String getValue(Object property) {
		String w = getWorldName();
		String value;
		if ((value = Configs.config.getString("per-" + Shared.mainClass.getSeparatorType() + "-settings." + w + ".Users." + getName() + "." + property)) != null) return value;
		if ((value = Configs.config.getString("Users." + getName() + "." + property)) != null) return value;
		if ((value = Configs.config.getString("per-" + Shared.mainClass.getSeparatorType() + "-settings." + w + ".Groups." + permissionGroup + "." + property)) != null) return value;
		if ((value = Configs.config.getString("per-" + Shared.mainClass.getSeparatorType() + "-settings." + w + ".Groups._OTHER_." + property)) != null) return value;
		if ((value = Configs.config.getString("Groups." + permissionGroup + "." + property)) != null) return value;
		if ((value = Configs.config.getString("Groups._OTHER_." + property)) != null) return value;
		return "";
	}
	private void updateRawHeaderAndFooter() {
		String rawHeader = "";
		String rawFooter = "";
		List<Object> h = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Users." + getName() + ".header");
		if (h == null) h = Configs.config.getList("Users." + getName() + ".header");
		if (h == null) h = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Groups." + permissionGroup + ".header");
		if (h == null) h = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".header");
		if (h == null) h = Configs.config.getList("Groups." + permissionGroup + ".header");
		if (h == null) h = Configs.config.getList("header");
		if (h == null) h = new ArrayList<Object>();
		List<Object> f = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Users." + getName() + ".footer");
		if (f == null) f = Configs.config.getList("Users." + getName() + ".footer");
		if (f == null) f = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Groups." + permissionGroup + ".footer");
		if (f == null) f = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".footer");
		if (f == null) f = Configs.config.getList("Groups." + permissionGroup + ".footer");
		if (f == null) f = Configs.config.getList("footer");
		if (f == null) f = new ArrayList<Object>();
		int i = 0;
		for (Object headerLine : h) {
			if (++i > 1) rawHeader += "\n§r";
			rawHeader += headerLine;
		}
		i = 0;
		for (Object footerLine : f) {
			if (++i > 1) rawFooter += "\n§r";
			rawFooter += footerLine;
		}
		setProperty("header", rawHeader);
		setProperty("footer", rawFooter);
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
			if (Playerlist.enable) {
				name = properties.get("tabprefix").get();
			} else {
				if (!NameTag16.enable && !Configs.unlimitedTags) {
					return getName();
				}
				name = properties.get("tagprefix").get();
			}
		}
		if (name == null || name.equals("")) {
			name = "§f";
		} else {
			name = Placeholders.replaceAllPlaceholders(name, this);
		}
		if (name.length() > 12) {
			name = name.substring(0, 12);
		}
		name += getName();
		if (name.length() > 15) {
			name = name.substring(0, 15);
		}
		for (int i = 1; i <= 255; ++i) {
			String name2 = name + (char)i;
			boolean nameUsed = false;
			for (ITabPlayer d : Shared.getPlayers()) {
				if (d.getTeamName() != null && d.getTeamName().equals(name2) && !d.getName().equals(getName())) {
					nameUsed = true;
				}
			}
			if (!nameUsed) {
				return name2;
			}
		}
		return getName();
	}
	public void updateTeamPrefixSuffix() {
		if (disabledNametag) return;
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		boolean tagprefixUpdate = tagprefix.isUpdateNeeded();
		boolean tagsuffixUpdate = tagsuffix.isUpdateNeeded();
		boolean collision = getTeamPush();
		if (tagprefixUpdate || tagsuffixUpdate || lastCollision != collision) {
			String replacedPrefix = tagprefix.get();
			String replacedSuffix = tagsuffix.get();
			lastCollision = collision;
			for (ITabPlayer all : Shared.getPlayers()) {
				if (Placeholders.placeholderAPI) {
					if (tagprefix.hasRelationalPlaceholders()) replacedPrefix = Placeholders.setRelational(this, all, replacedPrefix);
					if (tagsuffix.hasRelationalPlaceholders()) replacedSuffix = Placeholders.setRelational(this, all, replacedSuffix);
				}
				PacketAPI.updateScoreboardTeamPrefixSuffix(all, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush());
			}
		}
	}
	public void registerTeam() {
		if (disabledNametag) return;
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		String replacedPrefix = tagprefix.get();
		String replacedSuffix = tagsuffix.get();
		for (ITabPlayer all : Shared.getPlayers()) {
			if (Placeholders.placeholderAPI) {
				if (tagprefix.hasRelationalPlaceholders()) replacedPrefix = Placeholders.setRelational(this, all, replacedPrefix);
				if (tagsuffix.hasRelationalPlaceholders()) replacedSuffix = Placeholders.setRelational(this, all, replacedSuffix);
			}
			PacketAPI.registerScoreboardTeam(all, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush(), Lists.newArrayList(getName()));
		}
	}
	public void registerTeam(ITabPlayer to) {
		if (disabledNametag) return;
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		String replacedPrefix = tagprefix.get();
		String replacedSuffix = tagsuffix.get();
		if (Placeholders.placeholderAPI) {
			if (tagprefix.hasRelationalPlaceholders()) replacedPrefix = Placeholders.setRelational(this, to, replacedPrefix);
			if (tagsuffix.hasRelationalPlaceholders()) replacedSuffix = Placeholders.setRelational(this, to, replacedSuffix);
		}
		PacketAPI.registerScoreboardTeam(to, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush(), Lists.newArrayList(getName()));
	}
	private void unregisterTeam(ITabPlayer to) {
		PacketAPI.unregisterScoreboardTeam(to, teamName);
	}
	public void unregisterTeam() {
		for (ITabPlayer p : Shared.getPlayers()) unregisterTeam(p);
	}
	public void onWorldChange(String from, String to) {
		disabledHeaderFooter = Configs.disabledHeaderFooter.contains(to);
		disabledTablistNames = Configs.disabledTablistNames.contains(to);
		disabledNametag = Configs.disabledNametag.contains(to);
		disabledTablistObjective = Configs.disabledTablistObjective.contains(to);
		disabledBossbar = Configs.disabledBossbar.contains(to);
		updateGroupIfNeeded();
		updateAll();
		restartArmorStands();
		if (disabledBossbar) {
			for (BossBarLine line : BossBar.lines)
				PacketAPI.removeBossBar(this, line);
		} else for (BossBarLine active : getActiveBossBars()) {
			if (!BossBar.defaultBars.contains(active.getName())) { //per-world bar from previous world
				PacketAPI.removeBossBar(this, active);
			}
		}
		detectBossBarsAndSend();
		if (HeaderFooter.enable) {
			if (disabledHeaderFooter) {
				sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("",""));
			} else {
				HeaderFooter.refreshHeaderFooter(this);
			}
		}
		if (NameTag16.enable || Configs.unlimitedTags) {
			if (disabledNametag) {
				unregisterTeam();
			} else if (Configs.disabledNametag.contains(from)){
				registerTeam();
			} else {
				updateTeam();
			}
		}
		if (Playerlist.enable) updatePlayerListName(true);
		if (TabObjective.type != TabObjectiveType.NONE) {
			if (disabledTablistObjective && !Configs.disabledTablistObjective.contains(from)) {
				TabObjective.unload(this);
			}
			if (!disabledTablistObjective && Configs.disabledTablistObjective.contains(from)) {
				TabObjective.playerJoin(this);
			}
		}
		if (ScoreboardManager.enabled) {
			ScoreboardManager.unregister(this);
			ScoreboardManager.register(this);
		}
	}
	public void detectBossBarsAndSend() {
		activeBossBars.clear();
		if (disabledBossbar || !bossbarVisible) return;
		for (String defaultBar : BossBar.defaultBars) {
			BossBarLine bar = BossBar.getLine(defaultBar);
			if (bar != null && bar.hasPermission(this)) {
				PacketAPI.createBossBar(this, bar);
				activeBossBars.add(bar);
			}
		}
		for (String announcement : BossBar.announcements) {
			BossBarLine bar = BossBar.getLine(announcement);
			if (bar != null && bar.hasPermission(this)) {
				PacketAPI.createBossBar(this, bar);
			}
		}
		if (BossBar.perWorld.get(getWorldName()) != null)
			for (String worldbar : BossBar.perWorld.get(getWorldName())) {
				BossBarLine bar = BossBar.getLine(worldbar);
				if (bar != null && bar.hasPermission(this)) {
					PacketAPI.createBossBar(this, bar);
					activeBossBars.add(bar);
				}
			}
	}
	public void sendCustomPacket(UniversalPacketPlayOut packet) {
		try {
			sendPacket(Shared.mainClass.toNMS(packet, version));
		} catch (Exception e) {
			Shared.error("An error occured when creating " + getClass().getSimpleName(), e);
		}
	}
	public void sendCustomPacket(PacketPlayOut packet) {
		try {
			sendPacket(packet.toNMS(version));
		} catch (Exception e) {
			Shared.error("An error occured when creating " + getClass().getSimpleName(), e);
		}
	}
}