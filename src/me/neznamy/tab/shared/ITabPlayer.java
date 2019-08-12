package me.neznamy.tab.shared;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import io.netty.channel.Channel;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.bukkit.NameTagLineManager;
import me.neznamy.tab.bukkit.packets.ArmorStand;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.Scoreboard;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.BossBar.BossBarLine;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

public abstract class ITabPlayer{

	public Object player;
	public HashMap<Object, String> originalproperties = new HashMap<Object, String>();
	public HashMap<Object, String> temporaryproperties = new HashMap<Object, String>();

	private String group;
	private long lastRefreshGroup;
	private int lastTabObjectiveValue;
	public String teamName;
	private String rawHeader;
	private String rawFooter;
	private String lastReplacedHeader = "";
	private String lastReplacedFooter = "";
	private String rank;
	public String replacedTabFormat = "";
	private boolean isStaff;
	public List<ArmorStand> armorStands = new ArrayList<ArmorStand>();
	public ProtocolVersion version = ProtocolVersion.UNKNOWN; //preventing errors before this is loaded
	public Channel channel;
	public String ipAddress = "-";
	public boolean nameTagVisible = true;
	public boolean bossbarVisible = true;

	public boolean disabledHeaderFooter;
	public boolean disabledTablistNames;
	public boolean disabledNametag;
	public boolean disabledTablistObjective;
	public boolean disabledBossbar;
	
	public Scoreboard activeScoreboard;
	public boolean hiddenScoreboard;
	public boolean previewingNametag;


	//bukkit only
	public abstract Integer getEntityId();
	public abstract void restartArmorStands();
	public abstract void onJoin() throws Exception;
	public abstract String getMoney();
	public abstract void setTeamVisible(boolean p0);
	public abstract int getHealth();
	public abstract String getNickname();

	//per-type
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


	public ITabPlayer(Object player) {
		this.player = player;
		updateGroupIfNeeded();
		updateAll();
		if (NameTag16.enable || Configs.unlimitedTags) teamName = buildTeamName();
	}
	public void updatePlayerListName(boolean force) {
		getGroup();
		String newFormat = getTabFormat();
		if (newFormat.equals(getName())) {
			newFormat = "§f" + getName();
		} else {
			newFormat = Placeholders.replace(newFormat, this);
		}
		if (force || replacedTabFormat == null || !newFormat.equals(replacedTabFormat) || newFormat.contains("%rel_")) {
			replacedTabFormat = newFormat;
			setPlayerListName();
		}
	}
	public String getTabFormat(ITabPlayer other) {
		String format = replacedTabFormat;
		if (Placeholders.relationalPlaceholders) {
			return PlaceholderAPI.setRelationalPlaceholders((Player) player, (Player)other.getPlayer(), format);
		}
		return format;
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
				as.setNameFormat(getActiveProperty(as.getID()));
			}
			NameTagLineManager.refreshNames(this);
		}
	}
	public boolean getTeamVisibility() {
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
	public String getTabFormat() {
		return getActiveProperty("tabprefix") + getActiveProperty("customtabname") + getActiveProperty("tabsuffix");
	}
	public String getTagFormat() {
		return getActiveProperty("tagprefix") + getActiveProperty("customtagname") + getActiveProperty("tagsuffix");
	}
	public String getOriginalProperty(Object line) {
		return originalproperties.get(line);
	}
	public String getActiveProperty(Object line) {
		if (line.equals("nametag")) return getTagFormat();
		String value = getTemporaryProperty(line) != null ? getTemporaryProperty(line) : getOriginalProperty(line);
		if ((line+"").contains("custom") && (value == null || value.length() == 0)) return getName();
		return value;
	}
	public String getTemporaryProperty(Object line) {
		return temporaryproperties.get(line);
	}
	public void updateAll() {
		originalproperties.put("tabprefix", getValue("tabprefix"));
		originalproperties.put("tagprefix", getValue("tagprefix"));
		originalproperties.put("tabsuffix", getValue("tabsuffix"));
		originalproperties.put("tagsuffix", getValue("tagsuffix"));
		originalproperties.put("customtabname", getValue("customtabname"));
		originalproperties.put("customtagname", getValue("customtagname"));
		for (Object property : Premium.dynamicLines) {
			if (!property.equals("nametag")) originalproperties.put(property, getValue(property));
		}
		for (String property : Premium.staticLines.keySet()) {
			if (!property.equals("nametag")) originalproperties.put(property, getValue(property));
		}
		rank = (String) Configs.rankAliases.get("_OTHER_");
		for (Entry<String, Object> entry : Configs.rankAliases.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(group)) {
				rank = (String) entry.getValue();
				break;
			}
		}
		if (rank == null || rank.length() == 0) {
			rank = group;
		}
		isStaff = hasPermission("tab.staff");
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
	private void unregisterTeam(ITabPlayer to) {
		PacketAPI.unregisterScoreboardTeam(to, teamName);
	}
	public void unregisterTeam() {
		for (ITabPlayer p : Shared.getPlayers()) unregisterTeam(p);
	}
	public String getTeamName() {
		return teamName;
	}
	public String getRank() {
		if (rank == null) return "§7No Rank";
		return rank;
	}
	public int getLastTabObjectiveValue() {
		return lastTabObjectiveValue;
	}
	public void setLastTabObjectiveValue(int value) {
		lastTabObjectiveValue = value;
	}
	public String getRawHeader() {
		return rawHeader;
	}
	public String getRawFooter() {
		return rawFooter;
	}
	public String getLastHeader() {
		return lastReplacedHeader;
	}
	public String getLastFooter() {
		return lastReplacedFooter;
	}
	public void setLastHeader(String header) {
		lastReplacedHeader = header;
	}
	public void setLastFooter(String footer) {
		lastReplacedFooter = footer;
	}
	public boolean isStaff() {
		return isStaff;
	}
	private boolean getTeamPush() {
		return Configs.collision;
	}
	public void updateRawHeaderAndFooter() {
		rawHeader = "";
		rawFooter = "";
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
	}
	public String buildTeamName() {
		if (Premium.is()) {
			return Premium.sortingType.getTeamName(this);
		}
		String name = null;
		if (!Configs.sortByNickname) {
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
					name = getActiveProperty("tabprefix");
				} else {
					if (!NameTag16.enable && !Configs.unlimitedTags) {
						return getName();
					}
					name = getActiveProperty("tagprefix");
				}
			}
			if (name == null || name.equals("")) {
				name = "§f";
			} else {
				name = Placeholders.replace(name, this);
			}
			if (name.length() > 12) {
				name = name.substring(0, 12);
			}
			name += getName();
		} else {
			name = getNickname();
		}
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
		String[] replaced = Placeholders.replaceMultiple(this, getActiveProperty("tagprefix"), getActiveProperty("tagsuffix"));
		for (ITabPlayer all : Shared.getPlayers()) {
			String replacedPrefix = replaced[0];
			String replacedSuffix = replaced[1];
			if (Placeholders.relationalPlaceholders) {
				replacedPrefix = PlaceholderAPI.setRelationalPlaceholders((Player)all.getPlayer(), (Player) getPlayer(), replacedPrefix);
				replacedSuffix = PlaceholderAPI.setRelationalPlaceholders((Player)all.getPlayer(), (Player) getPlayer(), replacedSuffix);
			}
			PacketAPI.updateScoreboardTeamPrefixSuffix(all, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush());
		}
	}
	public void registerTeam() {
		if (disabledNametag) return;
		unregisterTeam();
		String[] replaced = Placeholders.replaceMultiple(this, getActiveProperty("tagprefix"), getActiveProperty("tagsuffix"));
		String replacedPrefix = replaced[0];
		String replacedSuffix = replaced[1];
		for (ITabPlayer all : Shared.getPlayers()) {
			if (Placeholders.relationalPlaceholders) {
				replacedPrefix = PlaceholderAPI.setRelationalPlaceholders((Player)all.getPlayer(), (Player) getPlayer(), replacedPrefix);
				replacedSuffix = PlaceholderAPI.setRelationalPlaceholders((Player)all.getPlayer(), (Player) getPlayer(), replacedSuffix);
			}
			PacketAPI.registerScoreboardTeam(all, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush(), Lists.newArrayList(getName()));
		}
	}
	public void registerTeam(ITabPlayer to) {
		if (disabledNametag) return;
		unregisterTeam(to);
		String[] replaced = Placeholders.replaceMultiple(this, getActiveProperty("tagprefix"), getActiveProperty("tagsuffix"));
		String replacedPrefix = replaced[0];
		String replacedSuffix = replaced[1];
		if (Placeholders.relationalPlaceholders) {
			replacedPrefix = PlaceholderAPI.setRelationalPlaceholders((Player)to.getPlayer(), (Player) getPlayer(), replacedPrefix);
			replacedSuffix = PlaceholderAPI.setRelationalPlaceholders((Player)to.getPlayer(), (Player) getPlayer(), replacedSuffix);
		}
		PacketAPI.registerScoreboardTeam(to, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush(), Lists.newArrayList(getName()));
	}
	public ProtocolVersion getVersion() {
		return version;
	}
	public Channel getChannel() {
		if (channel == null) loadChannel();
		return channel;
	}
	public String getIPAddress() {
		return ipAddress;
	}
	public void onWorldChange(String from, String to) {
		disabledHeaderFooter = Configs.disabledHeaderFooter.contains(to);
		disabledTablistNames = Configs.disabledTablistNames.contains(to);
		disabledNametag = Configs.disabledNametag.contains(to);
		disabledTablistObjective = Configs.disabledTablistObjective.contains(to);
		disabledBossbar = Configs.disabledBossbar.contains(to);
		updateGroupIfNeeded();
		updateAll();
		if (BossBar.enable) {
			if (disabledBossbar) {
				for (BossBarLine line : BossBar.lines) PacketAPI.removeBossBar(this, line.getBossBar());
			}
			if (!disabledBossbar && Configs.disabledBossbar.contains(from)) {
				for (BossBarLine line : BossBar.lines) BossBar.sendBar(this, line);
			}
		}
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
			} else {
				registerTeam();
			}
		}
		if (Shared.mainClass.listNames()) updatePlayerListName(false);
		if (TabObjective.type != TabObjectiveType.NONE) {
			if (disabledTablistObjective && !Configs.disabledTablistObjective.contains(from)) {
				TabObjective.unload(this);
			}
			if (!disabledTablistObjective && Configs.disabledTablistObjective.contains(from)) {
				TabObjective.playerJoin(this);
			}
		}
		if (ScoreboardManager.enabled) {
			ScoreboardManager.playerQuit(this);
			ScoreboardManager.playerJoin(this);
		}
	}
	public void setActiveScoreboard(Scoreboard board) {
		activeScoreboard = board;
	}
	public Scoreboard getActiveScoreboard() {
		return activeScoreboard;
	}
}