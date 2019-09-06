package me.neznamy.tab.shared;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import io.netty.channel.Channel;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.bukkit.unlimitedtags.ArmorStand;
import me.neznamy.tab.bukkit.unlimitedtags.NameTagLineManager;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.Scoreboard;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.BossBar.BossBarLine;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

public abstract class ITabPlayer{

	public Object player;
	public HashMap<String, Property> properties = new HashMap<String, Property>();
	private String group;
	private long lastRefreshGroup;
	public String teamName;
	private String rank;
	public List<ArmorStand> armorStands = new ArrayList<ArmorStand>();
	public ProtocolVersion version = ProtocolVersion.SERVER_VERSION; //preventing errors before this is loaded
	public Channel channel;
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

	public boolean fullyLoaded;


	//bukkit only
	public String getNickname() {return getName();}
	public String getMoney() {return "-";}
	public void setTeamVisible(boolean p0) {}
	public void restartArmorStands() {}
	public Integer getEntityId() {return 0;}
	public int getHealth() {return 0;}
	public boolean hasInvisibility() {return false;}

	//per-type
	public abstract void onJoin();
	public abstract void setPlayerListName();
	public abstract String getGroupFromPermPlugin();
	public abstract String[] getGroupsFromPermPlugin();
	public abstract boolean hasPermission(String permission);
	public abstract String getName();
	public abstract String getWorldName();
	public abstract UUID getUniqueId();
	public abstract Object getPlayer();
	public abstract long getPing();
	public abstract void sendPacket(Object nmsPacket);
	public abstract void sendMessage(String message);
	protected abstract void loadChannel();

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
		if (!fullyLoaded) return;
		getGroup();
		boolean tabprefix = properties.get("tabprefix").isUpdateNeeded();
		boolean customtabname = properties.get("customtabname").isUpdateNeeded();
		boolean tabsuffix = properties.get("tabsuffix").isUpdateNeeded();
		if (tabprefix || customtabname || tabsuffix || force) {
			setPlayerListName();
		}
	}
	public String getTabFormat(ITabPlayer other) {
		String format = properties.get("tabprefix").get() + properties.get("customtabname").get() + properties.get("tabsuffix").get();
		if (Placeholders.placeholderAPI) {
			return PlaceholderAPI.setRelationalPlaceholders((Player) player, (Player)other.getPlayer(), format);
		}
		return format;
	}
	public void updateTeam() {
		if (disabledNametag || !fullyLoaded) return;
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
		return group;
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
		if (newGroup != null && (group == null || !group.equals(newGroup))) {
			group = newGroup;
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
			if (entry.getKey().equalsIgnoreCase(group)) {
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
		if ((value = Configs.config.getString("per-" + Shared.mainClass.getSeparatorType() + "-settings." + w + ".Groups." + group + "." + property)) != null) return value;
		if ((value = Configs.config.getString("per-" + Shared.mainClass.getSeparatorType() + "-settings." + w + ".Groups._OTHER_." + property)) != null) return value;
		if ((value = Configs.config.getString("Groups." + group + "." + property)) != null) return value;
		if ((value = Configs.config.getString("Groups._OTHER_." + property)) != null) return value;
		return "";
	}
	public String getTeamName() {
		return teamName;
	}
	public String getRank() {
		if (rank == null) return "§7No Rank";
		return rank;
	}
	public boolean isStaff() {
		return hasPermission("tab.staff");
	}
	private boolean getTeamPush() {
		return Configs.collision;
	}
	private void updateRawHeaderAndFooter() {
		String rawHeader = "";
		String rawFooter = "";
		List<Object> h = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Users." + getName() + ".header");
		if (h == null) h = Configs.config.getList("Users." + getName() + ".header");
		if (h == null) h = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Groups." + group + ".header");
		if (h == null) h = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".header");
		if (h == null) h = Configs.config.getList("Groups." + group + ".header");
		if (h == null) h = Configs.config.getList("header");
		if (h == null) h = new ArrayList<Object>();
		List<Object> f = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Users." + getName() + ".footer");
		if (f == null) f = Configs.config.getList("Users." + getName() + ".footer");
		if (f == null) f = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Groups." + group + ".footer");
		if (f == null) f = Configs.config.getList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".footer");
		if (f == null) f = Configs.config.getList("Groups." + group + ".footer");
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
			for (String group : Configs.sortedGroups.keySet()) {
				if (hasPermission("tab.sort." + group)) {
					name = Configs.sortedGroups.get(group);
					break;
				}
			}
		} else {
			for (String group : Configs.sortedGroups.keySet()) {
				if (group.equalsIgnoreCase(this.group)) {
					name = Configs.sortedGroups.get(group);
					break;
				}
			}
		}
		if (name == null) {
			if (Shared.mainClass.listNames()) {
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
	public List<ArmorStand> getArmorStands() {
		return armorStands;
	}
	public void updateTeamPrefixSuffix() {
		if (disabledNametag) return;
		Property tagprefix = properties.get("tagprefix");
		Property tagsuffix = properties.get("tagsuffix");
		boolean tagprefixUpdate = tagprefix.isUpdateNeeded();
		boolean tagsuffixUpdate = tagsuffix.isUpdateNeeded();
		if (tagprefixUpdate || tagsuffixUpdate) {
			String replacedPrefix = tagprefix.get();
			String replacedSuffix = tagsuffix.get();
			for (ITabPlayer all : Shared.getPlayers()) {
				if (Placeholders.placeholderAPI) {
					if (tagprefix.hasRelationalPlaceholders()) replacedPrefix = PlaceholderAPI.setRelationalPlaceholders((Player)all.getPlayer(), (Player) getPlayer(), replacedPrefix);
					if (tagsuffix.hasRelationalPlaceholders()) replacedSuffix = PlaceholderAPI.setRelationalPlaceholders((Player)all.getPlayer(), (Player) getPlayer(), replacedSuffix);
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
				if (tagprefix.hasRelationalPlaceholders()) replacedPrefix = PlaceholderAPI.setRelationalPlaceholders((Player)all.getPlayer(), (Player) getPlayer(), replacedPrefix);
				if (tagsuffix.hasRelationalPlaceholders()) replacedSuffix = PlaceholderAPI.setRelationalPlaceholders((Player)all.getPlayer(), (Player) getPlayer(), replacedSuffix);
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
			if (tagprefix.hasRelationalPlaceholders()) replacedPrefix = PlaceholderAPI.setRelationalPlaceholders((Player)to.getPlayer(), (Player) getPlayer(), replacedPrefix);
			if (tagsuffix.hasRelationalPlaceholders()) replacedSuffix = PlaceholderAPI.setRelationalPlaceholders((Player)to.getPlayer(), (Player) getPlayer(), replacedSuffix);
		}
		PacketAPI.registerScoreboardTeam(to, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush(), Lists.newArrayList(getName()));
	}
	private void unregisterTeam(ITabPlayer to) {
		PacketAPI.unregisterScoreboardTeam(to, teamName);
	}
	public void unregisterTeam() {
		for (ITabPlayer p : Shared.getPlayers()) unregisterTeam(p);
	}
	public ProtocolVersion getVersion() {
		return version;
	}
	public Channel getChannel() {
		if (channel == null) loadChannel();
		return channel;
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
				new PacketPlayOutPlayerListHeaderFooter("","").send(this);
			} else {
				HeaderFooter.refreshHeaderFooter(this);
			}
		}
		if (NameTag16.enable || Configs.unlimitedTags) {
			if (disabledNametag) {
				unregisterTeam();
			} else if (Configs.disabledNametag.contains(from)){
				registerTeam();
			}
		}
		if (Shared.mainClass.listNames()) updatePlayerListName(true);
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
	public void setActiveScoreboard(Scoreboard board) {
		activeScoreboard = board;
	}
	public Scoreboard getActiveScoreboard() {
		return activeScoreboard;
	}
	public List<BossBarLine> getActiveBossBars(){
		return activeBossBars;
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
}