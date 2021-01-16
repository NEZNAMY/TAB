package me.neznamy.tab.shared;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.netty.channel.Channel;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.EnumProperty;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.command.level1.PlayerCommand;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

/**
 * The core class for player
 */
public abstract class ITabPlayer implements TabPlayer {

	protected String name;
	protected UUID uniqueId;
	protected String world;
	private String permissionGroup = "< Not Initialized Yet >";
	private String teamName;
	private String teamNameNote;

	private Map<String, Property> properties = new HashMap<String, Property>();
	private ArmorStandManager armorStandManager;
	protected ProtocolVersion version = ProtocolVersion.SERVER_VERSION;
	protected Channel channel;
	private boolean bossbarVisible;

	private boolean previewingNametag;
	private Set<BossBar> activeBossBars = new HashSet<BossBar>();
	private boolean collision;
	private Boolean forcedCollision;
	private boolean onJoinFinished;
	private boolean hiddenNametag;
	private Set<UUID> hiddenNametagFor = new HashSet<UUID>();
	private boolean onBoat;

	private Scoreboard activeScoreboard;
	private boolean scoreboardVisible;
	private Scoreboard forcedScoreboard;
	
	protected Map<String, String> attributes = new HashMap<String, String>();

	public void init() {
		setGroup(((GroupRefresher)TAB.getInstance().getFeatureManager().getFeature("group")).detectPermissionGroup(this), false);
	}

	public abstract boolean hasPermission(String permission);

	public abstract long getPing();

	public abstract void sendPacket(Object nmsPacket);

	public void sendMessage(String message, boolean translateColors) {
		if (message == null || message.length() == 0) return;
		IChatBaseComponent component;
		if (translateColors) {
			component = IChatBaseComponent.fromColoredText(message);
		} else {
			component = new IChatBaseComponent(message);
		}
		sendCustomPacket(new PacketPlayOutChat(component));
	}
	
	public void sendMessage(IChatBaseComponent message) {
		sendCustomPacket(new PacketPlayOutChat(message));
	}

	public abstract Object getSkin();

	private boolean getTeamVisibility(TabPlayer viewer) {
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx") && !onBoat) return false;
		if (hiddenNametag || (boolean) TAB.getInstance().getConfiguration().getSecretOption("invisible-nametags", false) || hiddenNametagFor.contains(viewer.getUniqueId())) return false;
		return !TAB.getInstance().getFeatureManager().getNameTagFeature().getInvisiblePlayers().contains(getName());
	}

	public void setProperty(String identifier, String rawValue, String source) {
		Property p = getProperty(identifier);
		if (p == null) {
			properties.put(identifier, new Property(this, rawValue, source));
		} else {
			p.changeRawValue(rawValue);
			p.setSource(source);
		}
	}


	/*
	 *  Implementing interface
	 */

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UUID getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public UUID getTablistUUID() {
		return uniqueId;
	}

	@Override
	public void setValueTemporarily(EnumProperty type, String value) {
		TAB.getInstance().getPlaceholderManager().checkForRegistration(value);
		Property pr = getProperty(type.toString());
		if (pr == null) throw new IllegalStateException("Feature handling this property is not enabled");
		pr.setTemporaryValue(value);
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx") && type.toString().contains("tag")) {
			setProperty("nametag",getProperty("tagprefix").getCurrentRawValue() + getProperty("customtagname").getCurrentRawValue() + getProperty("tagsuffix").getCurrentRawValue(), null);
		}
		forceRefresh();
	}

	@Override
	public void setValuePermanently(EnumProperty type, String value) {
		TAB.getInstance().getPlaceholderManager().checkForRegistration(value);
		((PlayerCommand)TAB.getInstance().command.subcommands.get("player")).savePlayer(null, getName(), type.toString(), value);
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx") && type.toString().contains("tag")) {
			setProperty("nametag", getProperty("tagprefix").getCurrentRawValue() + getProperty("customtagname").getCurrentRawValue() + getProperty("tagsuffix").getCurrentRawValue(), null);
		}
		Property pr = getProperty(type.toString());
		if (pr == null) return;
		pr.changeRawValue(value);
		forceRefresh();
	}

	@Override
	public String getTemporaryValue(EnumProperty type) {
		return getProperty(type.toString()).getTemporaryValue();
	}

	@Override
	public boolean hasTemporaryValue(EnumProperty type) {
		return getTemporaryValue(type) != null;
	}

	@Override
	public void removeTemporaryValue(EnumProperty type) {
		setValueTemporarily(type, null);
	}

	@Override
	public String getOriginalValue(EnumProperty type) {
		return getProperty(type.toString()).getOriginalRawValue();
	}

	@Override
	public void hideNametag() {
		hiddenNametag = true;
		updateTeamData();
	}

	@Override
	public void showNametag() {
		hiddenNametag = false;
		updateTeamData();
	}

	@Override
	public boolean hasHiddenNametag() {
		return hiddenNametag;
	}

	@Override
	public void forceRefresh() {
		TAB.getInstance().getFeatureManager().refresh(this, true);
	}

	@Override
	public void showScoreboard(Scoreboard scoreboard) {
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

	@Override
	public void showScoreboard(String name) {
		ScoreboardManager sbm = ((ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard"));
		if (sbm == null) throw new IllegalStateException("Scoreboard feature is not enabled");
		Scoreboard scoreboard = sbm.getScoreboards().get(name);
		if (scoreboard == null) throw new IllegalArgumentException("No scoreboard found with name: " + name);
		showScoreboard(scoreboard);
	}

	@Override
	public void removeCustomScoreboard() {
		if (forcedScoreboard == null) return;
		ScoreboardManager sbm = ((ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard"));
		if (sbm == null) throw new IllegalStateException("Scoreboard feature is not enabled");
		Scoreboard sb = sbm.getScoreboards().get(sbm.detectHighestScoreboard(this));
		if (sb == null) return; //no scoreboard available
		activeScoreboard = sb;
		sb.register(this);
		forcedScoreboard = null;
	}

	@Override
	public ProtocolVersion getVersion() {
		return version;
	}

	@Override
	public String getWorldName() {
		return world;
	}

	@Override
	public void setWorldName(String name) {
		world = name;
	}

	@Override
	public void sendCustomPacket(UniversalPacketPlayOut packet) {
		sendPacket(packet.create(getVersion()));
	}

	@Override
	public Property getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public String getGroup() {
		return permissionGroup;
	}

	@Override
	public void toggleNametagPreview() {
		if (armorStandManager == null) throw new IllegalStateException("Unlimited nametag mode is not enabled");
		if (previewingNametag) {
			armorStandManager.destroy(this);
			sendMessage(TAB.getInstance().getConfiguration().translation.getString("preview-off"), true);
		} else {
			armorStandManager.spawn(this);
			sendMessage(TAB.getInstance().getConfiguration().translation.getString("preview-on"), true);
		}
		previewingNametag = !previewingNametag;
	}

	@Override
	public boolean isPreviewingNametag() {
		return previewingNametag;
	}

	@Override
	public boolean isStaff() {
		return hasPermission("tab.staff");
	}

	@Override
	public Channel getChannel() {
		return channel;
	}

	@Override
	public void unregisterTeam() {
		if (teamName == null) return;
		Object packet = new PacketPlayOutScoreboardTeam(teamName).setTeamOptions(69).create(ProtocolVersion.SERVER_VERSION);
		for (TabPlayer viewer : TAB.getInstance().getPlayers()) {
			viewer.sendPacket(packet);
		}
	}

	@Override
	public void unregisterTeam(TabPlayer viewer) {
		viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName).setTeamOptions(69));
	}

	@Override
	public void registerTeam() {
		Property tagprefix = getProperty("tagprefix");
		Property tagsuffix = getProperty("tagsuffix");
		for (TabPlayer viewer : TAB.getInstance().getPlayers()) {
			String currentPrefix = tagprefix.getFormat(viewer);
			String currentSuffix = tagsuffix.getFormat(viewer);
			PacketAPI.registerScoreboardTeam(viewer, teamName, currentPrefix, currentSuffix, getTeamVisibility(viewer), collision, Arrays.asList(getName()), null);
		}
	}

	@Override
	public void registerTeam(TabPlayer viewer) {
		Property tagprefix = getProperty("tagprefix");
		Property tagsuffix = getProperty("tagsuffix");
		String replacedPrefix = tagprefix.getFormat(viewer);
		String replacedSuffix = tagsuffix.getFormat(viewer);
		PacketAPI.registerScoreboardTeam(viewer, teamName, replacedPrefix, replacedSuffix, getTeamVisibility(viewer), collision, Arrays.asList(getName()), null);
	}

	@Override
	public void updateTeam() {
		if (teamName == null) return; //player not loaded yet
		String newName = TAB.getInstance().getFeatureManager().getNameTagFeature().sorting.getTeamName(this);
		if (teamName.equals(newName)) {
			updateTeamData();
		} else {
			unregisterTeam();
			teamName = newName;
			registerTeam();
		}
	}

	@Override
	public void updateTeamData() {
		Property tagprefix = getProperty("tagprefix");
		Property tagsuffix = getProperty("tagsuffix");
		for (TabPlayer viewer : TAB.getInstance().getPlayers()) {
			String currentPrefix = tagprefix.getFormat(viewer);
			String currentSuffix = tagsuffix.getFormat(viewer);
			boolean visible = getTeamVisibility(viewer);
			viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, currentPrefix, currentSuffix, visible?"always":"never", collision?"always":"never", 69));
		}
	}

	private void updateTeamData(TabPlayer viewer) {
		Property tagprefix = getProperty("tagprefix");
		Property tagsuffix = getProperty("tagsuffix");
		boolean visible = getTeamVisibility(viewer);
		String currentPrefix = tagprefix.getFormat(viewer);
		String currentSuffix = tagsuffix.getFormat(viewer);
		viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, currentPrefix, currentSuffix, visible?"always":"never", collision?"always":"never", 69));
	}

	@Override
	public boolean isLoaded() {
		return onJoinFinished;
	}

	@Override
	public void markAsLoaded() {
		onJoinFinished = true;
	}

	@Override
	public boolean hasBossbarVisible() {
		return bossbarVisible;
	}

	@Override
	public void setBossbarVisible(boolean visible) {
		bossbarVisible = visible;
	}

	@Override
	public Set<BossBar> getActiveBossBars(){
		return activeBossBars;
	}

	@Override
	public void setProperty(String identifier, String rawValue) {
		setProperty(identifier, rawValue, null);
	}

	@Override
	public void loadPropertyFromConfig(String property) {
		loadPropertyFromConfig(property, "");
	}

	@Override
	public void loadPropertyFromConfig(String property, String ifNotSet) {
		String playerGroupFromConfig = permissionGroup.replace(".", "@#@");
		String worldGroup = TAB.getInstance().getConfiguration().getWorldGroupOf(getWorldName());
		String value;
		if ((value = TAB.getInstance().getConfiguration().config.getString("per-" + TAB.getInstance().getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Users." + getName() + "." + property)) != null) {
			setProperty(property, value, "Player: " + getName() + ", " + TAB.getInstance().getPlatform().getSeparatorType() + ": " + worldGroup);
			return;
		}
		if ((value = TAB.getInstance().getConfiguration().config.getString("per-" + TAB.getInstance().getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Users." + getUniqueId().toString() + "." + property)) != null) {
			setProperty(property, value, "PlayerUUID: " + getName() + ", " + TAB.getInstance().getPlatform().getSeparatorType() + ": " + worldGroup);
			return;
		}
		if ((value = TAB.getInstance().getConfiguration().config.getString("Users." + getName() + "." + property)) != null) {
			setProperty(property, value, "Player: " + getName());
			return;
		}
		if ((value = TAB.getInstance().getConfiguration().config.getString("Users." + getUniqueId().toString() + "." + property)) != null) {
			setProperty(property, value, "PlayerUUID: " + getName());
			return;
		}
		if ((value = TAB.getInstance().getConfiguration().config.getString("per-" + TAB.getInstance().getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Groups." + playerGroupFromConfig + "." + property)) != null) {
			setProperty(property, value, "Group: " + permissionGroup + ", " + TAB.getInstance().getPlatform().getSeparatorType() + ": " + worldGroup);
			return;
		}
		if ((value = TAB.getInstance().getConfiguration().config.getString("per-" + TAB.getInstance().getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Groups._OTHER_." + property)) != null) {
			setProperty(property, value, "Group: _OTHER_," + TAB.getInstance().getPlatform().getSeparatorType() + ": " + worldGroup);
			return;
		}
		if ((value = TAB.getInstance().getConfiguration().config.getString("Groups." + playerGroupFromConfig + "." + property)) != null) {
			setProperty(property, value, "Group: " + permissionGroup);
			return;
		}
		if ((value = TAB.getInstance().getConfiguration().config.getString("Groups._OTHER_." + property)) != null) {
			setProperty(property, value, "Group: _OTHER_");
			return;
		}
		setProperty(property, ifNotSet, "None");
	}

	@Override
	public ArmorStandManager getArmorStandManager() {
		return armorStandManager;
	}

	@Override
	public void setArmorStandManager(ArmorStandManager armorStandManager) {
		this.armorStandManager = armorStandManager;
	}

	@Override
	public void setTeamName(String name) {
		teamName = name;
	}

	@Override
	public String getTeamName() {
		return teamName;
	}

	@Override
	public void setTeamNameNote(String note) {
		teamNameNote = note;
	}

	@Override
	public String getTeamNameNote() {
		return teamNameNote;
	}

	@Override
	public void setCollisionRule(Boolean collision) {
		this.forcedCollision = collision;
	}

	@Override
	public Boolean getCollisionRule() {
		return forcedCollision;
	}

	@Override
	public void setOnBoat(boolean onBoat) {
		this.onBoat = onBoat;
	}

	@Override
	public boolean isOnBoat() {
		return onBoat;
	}


	@Override
	public void setScoreboardVisible(boolean visible, boolean sendToggleMessage) {
		if (scoreboardVisible == visible) return;
		scoreboardVisible = visible;
		ScoreboardManager scoreboardManager = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (scoreboardManager == null) throw new IllegalStateException("Scoreboard feature is not enabled");
		if (visible) {
			scoreboardManager.sendHighestScoreboard(this);
			if (sendToggleMessage) {
				sendMessage(scoreboardManager.scoreboard_on, true);
			}
			if (scoreboardManager.remember_toggle_choice) {
				scoreboardManager.sb_off_players.remove(getName());
				TAB.getInstance().getConfiguration().playerdata.set("scoreboard-off", scoreboardManager.sb_off_players);
			}
		} else {
			scoreboardManager.unregisterScoreboard(this, true);
			if (sendToggleMessage) {
				sendMessage(scoreboardManager.scoreboard_off, true);
			}
			if (scoreboardManager.remember_toggle_choice) {
				scoreboardManager.sb_off_players.add(getName());
				TAB.getInstance().getConfiguration().playerdata.set("scoreboard-off", scoreboardManager.sb_off_players);
			}
		}
	}

	@Override
	public void toggleScoreboard(boolean sendToggleMessage) {
		setScoreboardVisible(!scoreboardVisible, sendToggleMessage);
	}

	@Override
	public boolean isScoreboardVisible() {
		return scoreboardVisible;
	}

	@Override
	public void setActiveScoreboard(Scoreboard board) {
		activeScoreboard = board;
	}

	@Override
	public Scoreboard getActiveScoreboard() {
		return activeScoreboard;
	}

	@Override
	public void setGroup(String permissionGroup, boolean refreshIfChanged) {
		if (this.permissionGroup.equals(permissionGroup)) return;
		if (permissionGroup != null) {
			this.permissionGroup = permissionGroup;
		} else {
			this.permissionGroup = "<null>";
			TAB.getInstance().getErrorManager().oneTimeConsoleError(TAB.getInstance().getPermissionPlugin().getName() + " v" + TAB.getInstance().getPermissionPlugin().getVersion() + " returned null permission group for " + getName());
		}
		if (refreshIfChanged) {
			forceRefresh();
		}
	}

	@Override
	public boolean hasForcedScoreboard() {
		return forcedScoreboard != null;
	}

	@Override
	public boolean isVanished() {
		return false;
	}

	@Override
	public void showBossBar(BossBar bossbar) {
		BossBarLine line = (BossBarLine) bossbar;
		line.create(this);
		activeBossBars.add(line);
	}

	@Override
	public void removeBossBar(BossBar bossbar) {
		BossBarLine line = (BossBarLine) bossbar;
		line.remove(this);
		activeBossBars.remove(line);
	}

	public void hideNametag(UUID viewer) {
		if (hiddenNametagFor.add(viewer)) {
			updateTeamData(TAB.getInstance().getPlayer(viewer));
		}
	}

	public void showNametag(UUID viewer) {
		if (hiddenNametagFor.remove(viewer)) {
			updateTeamData(TAB.getInstance().getPlayer(viewer));
		}
	}

	public void updateCollision() {
		if (forcedCollision != null) {
			if (collision != forcedCollision) {
				collision = forcedCollision;
				updateTeamData();
			}
		} else {
			boolean collision = !isDisguised() && TAB.getInstance().getConfiguration().revertedCollision.contains(world) ? !TAB.getInstance().getConfiguration().collisionRule : TAB.getInstance().getConfiguration().collisionRule;
			if (this.collision != collision) {
				this.collision = collision;
				updateTeamData();
			}
		}
	}
	
	
	@Override
	public void setAttribute(String attribute, String value) {
		attributes.put(attribute, value);
	}
}