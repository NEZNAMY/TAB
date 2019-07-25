package me.neznamy.tab.shared;

import java.util.*;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import io.netty.channel.Channel;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.bukkit.NameTagLineManager;
import me.neznamy.tab.bukkit.objects.ArmorStand;

public abstract class ITabPlayer{

	public Object player;
	public String tagPrefix;
	public String tabPrefix;
	public String tagSuffix;
	public String tabSuffix;
	private String belowname;
	private String abovename;
	public String customtagname;
	public String customtabname;

	public String temporaryTagPrefix;
	public String temporaryTabPrefix;
	public String temporaryTagSuffix;
	public String temporaryTabSuffix;
	public String temporaryBelowName;
	public String temporaryAboveName;
	public String temporaryCustomTagName;
	public String temporaryCustomTabName;

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
	public int version;
	public Channel channel;
	public String ipAddress;
	public boolean nameTagVisible = true;
	public boolean bossbarVisible = true;


	//bukkit only
	public abstract Integer getEntityId();
	public abstract void restartArmorStands();
	public abstract void onJoin() throws Exception;
	public abstract String getMoney();
	public abstract void setTeamVisible(boolean p0);
	public abstract int getHealth();
	public abstract String getNickname();

	//per-type
	public abstract void setPlayerListName(String name);
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
			setPlayerListName(getName());
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
		if (Configs.disabledNametag.contains(getWorldName())) return;
		String newName = buildTeamName();
		if (teamName.equals(newName)) {
			updateTeamPrefixSuffix();
		} else {
			unregisterTeam();
			teamName = newName;
			registerTeam();
		}
		if (Configs.unlimitedTags) {
			if (NameTagLineManager.getByID(this, "NAMETAG") != null) {
				NameTagLineManager.getByID(this, "NAMETAG").setNameFormat(getTagFormat());
			}
			if (NameTagLineManager.getByID(this, "BELOWNAME") != null) {
				NameTagLineManager.getByID(this, "BELOWNAME").setNameFormat(getBelowName());
			}
			if (NameTagLineManager.getByID(this, "ABOVENAME") != null) {
				NameTagLineManager.getByID(this, "ABOVENAME").setNameFormat(getAboveName());
			}
			NameTagLineManager.replaceFormats(this);
			NameTagLineManager.updateMetadata(this);
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
			if (playerGroups != null)
				loop:
					for (String entry : Configs.primaryGroupFindingList) {
						for (String playerGroup : playerGroups) {
							if (playerGroup.equalsIgnoreCase(entry)) {
								newGroup = playerGroup;
								break loop;
							}
						}
					}
			if (newGroup == null) newGroup = playerGroups[0];
		}
		if (group == null || !group.equals(newGroup)) {
			group = newGroup;
			updateAll();
		}
	}
	public String getTabFormat() {
		return getTabPrefix() + getTabName() + getTabSuffix();
	}
	public String getTagFormat() {
		return getTagPrefix() + getTagName() + getTagSuffix();
	}
	public String getTabName() {
		if (temporaryCustomTabName != null) return temporaryCustomTabName;
		if (customtabname.length() > 0) return customtabname;
		return getName();
	}
	public String getTagName() {
		if (temporaryCustomTagName != null) return temporaryCustomTagName;
		if (customtagname.length() > 0) return customtagname;
		return getName();
	}
	public String getAboveName() {
		if (temporaryAboveName != null) return temporaryAboveName;
		return abovename;
	}
	public String getBelowName() {
		if (temporaryBelowName != null) return temporaryBelowName;
		return belowname;
	}
	public String getTabPrefix() {
		if (temporaryTabPrefix != null) return temporaryTabPrefix;
		return tabPrefix;
	}
	public String getTagPrefix() {
		if (temporaryTagPrefix != null) return temporaryTagPrefix;
		return tagPrefix;
	}
	public String getTabSuffix() {
		if (temporaryTabSuffix != null) return temporaryTabSuffix;
		return tabSuffix;
	}
	public String getTagSuffix() {
		if (temporaryTagSuffix != null) return temporaryTagSuffix;
		return tagSuffix;
	}
	public void updateAll() {
		tabPrefix = getValue("tabprefix");
		tagPrefix = getValue("tagprefix");
		tabSuffix = getValue("tabsuffix");
		tagSuffix = getValue("tagsuffix");
		belowname = getValue("belowname");
		abovename = getValue("abovename");
		customtabname = getValue("customtabname");
		customtagname = getValue("customtagname");
		for (Map.Entry<String, Object> entry : Configs.rankAliases.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(group)) {
				rank = (String) entry.getValue();
				break;
			}
		}
		if (rank == null) {
			rank = (String) Configs.rankAliases.get("_OTHER_");
		}
		if (rank == null || rank.length() == 0) {
			rank = group;
		}
		for (String staffGroup : Configs.staffGroups) {
			if (staffGroup.equalsIgnoreCase(group)) isStaff = true;
		}
		updateRawHeaderAndFooter();
	}
	private String getValue(String s) {
		String w = getWorldName();
		String value;
		if ((value = Configs.config.getString("per-" + Shared.mainClass.getSeparatorType() + "-settings." + w + ".Users." + getName() + "." + s)) != null) return value;
		if ((value = Configs.config.getString("Users." + getName() + "." + s)) != null) return value;
		if ((value = Configs.config.getString("per-" + Shared.mainClass.getSeparatorType() + "-settings." + w + ".Groups." + group + "." + s)) != null) return value;
		if ((value = Configs.config.getString("per-" + Shared.mainClass.getSeparatorType() + "-settings." + w + ".Groups._OTHER_." + s)) != null) return value;
		if ((value = Configs.config.getString("Groups." + group + "." + s)) != null) return value;
		if ((value = Configs.config.getString("Groups._OTHER_." + s)) != null) return value;
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
		List<String> h = Configs.config.getStringList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Users." + getName() + ".header");
		if (h == null) h = Configs.config.getStringList("Users." + getName() + ".header");
		if (h == null) h = Configs.config.getStringList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Groups." + group + ".header");
		if (h == null) h = Configs.config.getStringList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".header");
		if (h == null) h = Configs.config.getStringList("Groups." + group + ".header");
		if (h == null) h = Configs.config.getStringList("header");
		if (h == null) h = new ArrayList<String>();
		List<String> f = Configs.config.getStringList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Users." + getName() + ".footer");
		if (f == null) f = Configs.config.getStringList("Users." + getName() + ".footer");
		if (f == null) f = Configs.config.getStringList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".Groups." + group + ".footer");
		if (f == null) f = Configs.config.getStringList("per-" + Shared.mainClass.getSeparatorType() + "-settings." + getWorldName() + ".footer");
		if (f == null) f = Configs.config.getStringList("Groups." + group + ".footer");
		if (f == null) f = Configs.config.getStringList("footer");
		if (f == null) f = new ArrayList<String>();
		int i = 0;
		for (String a : h) {
			if (++i > 1) rawHeader += "\n§r";
			rawHeader += a;
		}
		i = 0;
		for (String a : f) {
			if (++i > 1) rawFooter += "\n§r";
			rawFooter += a;
		}
	}
	public String buildTeamName() {
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
					name = getTabPrefix();
				} else {
					if (!NameTag16.enable && !Configs.unlimitedTags) {
						return getName();
					}
					name = getTagPrefix();
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
		if (Configs.disabledNametag.contains(getWorldName())) return;
		String[] replaced = Placeholders.replaceMultiple(this, getTagPrefix(), getTagSuffix());
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
		if (Configs.disabledNametag.contains(getWorldName())) return;
		unregisterTeam();
		String[] replaced = Placeholders.replaceMultiple(this, getTagPrefix(), getTagSuffix());
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
		if (Configs.disabledNametag.contains(getWorldName())) return;
		unregisterTeam(to);
		String[] replaced = Placeholders.replaceMultiple(this, getTagPrefix(), getTagSuffix());
		String replacedPrefix = replaced[0];
		String replacedSuffix = replaced[1];
		if (Placeholders.relationalPlaceholders) {
			replacedPrefix = PlaceholderAPI.setRelationalPlaceholders((Player)to.getPlayer(), (Player) getPlayer(), replacedPrefix);
			replacedSuffix = PlaceholderAPI.setRelationalPlaceholders((Player)to.getPlayer(), (Player) getPlayer(), replacedSuffix);
		}
		PacketAPI.registerScoreboardTeam(to, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(), getTeamPush(), Lists.newArrayList(getName()));
	}
	public int getVersion() {
		return version;
	}
	public Channel getChannel() {
		if (channel == null) loadChannel();
		return channel;
	}
	public String getIPAddress() {
		return ipAddress;
	}
}