package me.neznamy.tab.shared;

import java.util.ArrayList;
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
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

/**
 * The core class for player
 */
public abstract class ITabPlayer implements TabPlayer {

	protected String name;
	protected UUID uniqueId;
	protected String world;
	private String permissionGroup = "<null>";
	private String teamName;
	private String teamNameNote;
	private String forcedTeamName;

	private Map<String, Property> properties = new HashMap<String, Property>();
	private ArmorStandManager armorStandManager;
	protected ProtocolVersion version = ProtocolVersion.SERVER_VERSION;
	protected Channel channel;
	private boolean bossbarVisible;

	private boolean previewingNametag;
	private Set<BossBar> activeBossBars = new HashSet<BossBar>();
	private Boolean forcedCollision;
	private boolean onJoinFinished;
	private boolean hiddenNametag;
	private Set<UUID> hiddenNametagFor = new HashSet<UUID>();

	private Scoreboard activeScoreboard;
	private boolean scoreboardVisible;
	private Scoreboard forcedScoreboard;
	private String otherPluginScoreboard;
	private boolean teamHandlingPaused;

	protected Map<String, String> attributes = new HashMap<String, String>();

	protected void init() {
		setGroup(((GroupRefresher)TAB.getInstance().getFeatureManager().getFeature("group")).detectPermissionGroup(this), false);
	}

	private void setProperty(String identifier, String rawValue, String source) {
		Property p = getProperty(identifier);
		if (p == null) {
			properties.put(identifier, new Property(this, rawValue, source));
		} else {
			p.changeRawValue(rawValue);
			p.setSource(source);
		}
	}
	
	@Override
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

	@Override
	public void sendMessage(IChatBaseComponent message) {
		sendCustomPacket(new PacketPlayOutChat(message));
	}

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
		TAB.getInstance().debug("Received API request to set property " + type + " of " + getName() + " temporarily to " + value + " by " + Thread.currentThread().getStackTrace()[2].toString());
		Property pr = getProperty(type.toString());
		if (pr == null) throw new IllegalStateException("Feature handling this property (" + type + ") is not enabled");
		pr.setTemporaryValue(value);
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx") && type.toString().contains("tag")) {
			setProperty("nametag",getProperty("tagprefix").getCurrentRawValue() + getProperty("customtagname").getCurrentRawValue() + getProperty("tagsuffix").getCurrentRawValue(), null);
		}
		TAB.getInstance().getPlaceholderManager().checkForRegistration(value);
		forceRefresh();
	}

	@Override
	public void setValuePermanently(EnumProperty type, String value) {
		TAB.getInstance().debug("Received API request to set property " + type + " of " + getName() + " permanently to " + value + " by " + Thread.currentThread().getStackTrace()[2].toString());
		((PlayerCommand)TAB.getInstance().command.subcommands.get("player")).savePlayer(null, getName(), type.toString(), value);
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx") && type.toString().contains("tag")) {
			setProperty("nametag", getProperty("tagprefix").getCurrentRawValue() + getProperty("customtagname").getCurrentRawValue() + getProperty("tagsuffix").getCurrentRawValue(), null);
		}
		Property pr = getProperty(type.toString());
		if (pr == null) return;
		pr.changeRawValue(value);
		TAB.getInstance().getPlaceholderManager().checkForRegistration(value);
		forceRefresh();
	}

	@Override
	public String getTemporaryValue(EnumProperty type) {
		Property pr = getProperty(type.toString());
		return pr == null ? null : pr.getTemporaryValue();
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
		if (TAB.getInstance().getFeatureManager().getNameTagFeature() == null) return;
		hiddenNametag = true;
		TAB.getInstance().getFeatureManager().getNameTagFeature().updateTeamData(this);
	}

	@Override
	public void showNametag() {
		if (TAB.getInstance().getFeatureManager().getNameTagFeature() == null) return;
		hiddenNametag = false;
		TAB.getInstance().getFeatureManager().getNameTagFeature().updateTeamData(this);
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

	public void setWorldName(String name) {
		world = name;
	}

	@Override
	public void sendCustomPacket(UniversalPacketPlayOut packet) {
		Object p = packet.create(getVersion());
		long time = System.nanoTime();
		sendPacket(p);
		TAB.getInstance().getCPUManager().addMethodTime("sendPacket", System.nanoTime()-time);
	}

	@Override
	public void sendCustomPacket(UniversalPacketPlayOut packet, TabFeature feature) {
		sendCustomPacket(packet);
		TAB.getInstance().getCPUManager().packetSent(feature);
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
	public boolean isLoaded() {
		return onJoinFinished;
	}

	public void markAsLoaded() {
		onJoinFinished = true;
	}

	@Override
	public boolean hasBossbarVisible() {
		return bossbarVisible;
	}

	@Override
	public void setBossbarVisible(boolean visible, boolean sendToggleMessage) {
		bossbarVisible = visible;
		me.neznamy.tab.shared.features.bossbar.BossBar feature = (me.neznamy.tab.shared.features.bossbar.BossBar) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (feature == null) return;
		if (hasBossbarVisible()) {
			feature.detectBossBarsAndSend(this);
			if (sendToggleMessage) sendMessage(TAB.getInstance().getConfiguration().translation.getString("bossbar-toggle-on"), true);
			if (feature.remember_toggle_choice) {
				feature.bossbar_off_players.remove(getName());
				TAB.getInstance().getConfiguration().playerdata.set("bossbar-off", feature.bossbar_off_players);
			}
		} else {
			for (BossBar line : getActiveBossBars().toArray(new BossBar[0])) {
				removeBossBar(line);
			}
			getActiveBossBars().clear();
			if (sendToggleMessage) sendMessage(TAB.getInstance().getConfiguration().translation.getString("bossbar-toggle-off"), true);
			if (feature.remember_toggle_choice && !feature.bossbar_off_players.contains(getName())) {
				feature.bossbar_off_players.add(getName());
				TAB.getInstance().getConfiguration().playerdata.set("bossbar-off", feature.bossbar_off_players);
			}
		}
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

	public void setTeamName(String name) {
		teamName = name;
	}

	@Override
	public String getTeamName() {
		if (forcedTeamName != null) return forcedTeamName;
		return teamName;
	}

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
	public void setScoreboardVisible(boolean visible, boolean sendToggleMessage) {
		if (scoreboardVisible == visible) return;
		scoreboardVisible = visible;
		ScoreboardManager scoreboardManager = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (scoreboardManager == null) return;
		if (visible) {
			scoreboardManager.sendHighestScoreboard(this);
			if (sendToggleMessage) {
				sendMessage(scoreboardManager.scoreboard_on, true);
			}
			if (scoreboardManager.remember_toggle_choice) {
				if (scoreboardManager.hiddenByDefault) {
					scoreboardManager.sb_off_players.add(getName());
				} else {
					scoreboardManager.sb_off_players.remove(getName());
				}
				synchronized (scoreboardManager.sb_off_players){
					TAB.getInstance().getConfiguration().playerdata.set("scoreboard-off", new ArrayList<>(scoreboardManager.sb_off_players));
				}
			}
		} else {
			scoreboardManager.unregisterScoreboard(this, true);
			if (sendToggleMessage) {
				sendMessage(scoreboardManager.scoreboard_off, true);
			}
			if (scoreboardManager.remember_toggle_choice) {
				if (scoreboardManager.hiddenByDefault) {
					scoreboardManager.sb_off_players.remove(getName());
				} else {
					scoreboardManager.sb_off_players.add(getName());
				}
				synchronized (scoreboardManager.sb_off_players){
					TAB.getInstance().getConfiguration().playerdata.set("scoreboard-off", new ArrayList<>(scoreboardManager.sb_off_players));
				}
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

	public void setActiveScoreboard(Scoreboard board) {
		activeScoreboard = board;
	}

	@Override
	public Scoreboard getActiveScoreboard() {
		return activeScoreboard;
	}

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

	@Override
	public void hideNametag(UUID viewer) {
		if (TAB.getInstance().getFeatureManager().getNameTagFeature() == null) return;
		if (hiddenNametagFor.add(viewer)) {
			TAB.getInstance().getFeatureManager().getNameTagFeature().updateTeamData(this, TAB.getInstance().getPlayer(viewer));
			if (armorStandManager != null) armorStandManager.updateVisibility(true);
		}
	}

	@Override
	public void showNametag(UUID viewer) {
		if (TAB.getInstance().getFeatureManager().getNameTagFeature() == null) return;
		if (hiddenNametagFor.remove(viewer)) {
			TAB.getInstance().getFeatureManager().getNameTagFeature().updateTeamData(this, TAB.getInstance().getPlayer(viewer));
			if (armorStandManager != null) armorStandManager.updateVisibility(true);
		}
	}

	@Override
	public boolean hasHiddenNametag(UUID viewer) {
		return hiddenNametagFor.contains(viewer);
	}

	@Override
	public void setAttribute(String attribute, String value) {
		attributes.put(attribute, value);
	}

	public void setOtherPluginScoreboard(String objectiveName) {
		this.otherPluginScoreboard = objectiveName;
	}

	public String getOtherPluginScoreboard() {
		return otherPluginScoreboard;
	}

	@Override
	public void sendPacket(Object nmsPacket, TabFeature feature) {
		sendPacket(nmsPacket);
		TAB.getInstance().getCPUManager().packetSent(feature);
	}
	
	@Override
	public void forceTeamName(String name) {
		if (String.valueOf(forcedTeamName).equals(name)) return;
		if (name != null && name.length() > 16) throw new IllegalArgumentException("Team name cannot be more than 16 characters long.");
		NameTag nametags = TAB.getInstance().getFeatureManager().getNameTagFeature();
		if (nametags == null) return;
		nametags.unregisterTeam(this);
		forcedTeamName = name;
		nametags.registerTeam(this);
		if (name != null) teamNameNote = "Set using API";
	}
	
	@Override
	public String getForcedTeamName() {
		return forcedTeamName;
	}
	
	@Override
	public void pauseTeamHandling() {
		if (teamHandlingPaused) return;
		NameTag f = TAB.getInstance().getFeatureManager().getNameTagFeature();
		if (f != null && !f.isDisabledWorld(getWorldName())) {
			f.unregisterTeam(this);
		}
		teamHandlingPaused = true; //setting to true after, so unregisterTeam method runs
	}

	@Override
	public void resumeTeamHandling() {
		if (!teamHandlingPaused) return;
		teamHandlingPaused = false; //setting to false before, so registerTeam method runs
		NameTag f = TAB.getInstance().getFeatureManager().getNameTagFeature();
		if (f != null && !f.isDisabledWorld(getWorldName())) {
			f.registerTeam(this);
		}
	}
	
	@Override
	public boolean hasTeamHandlingPaused() {
		return teamHandlingPaused;
	}
}