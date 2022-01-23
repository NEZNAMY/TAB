package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.UnlimitedNametagManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.NameTag;

/**
 * The core class for unlimited NameTag mode
 */
public class NameTagX extends NameTag implements UnlimitedNametagManager {

	//config options
	private final boolean markerFor18x = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.use-marker-tag-for-1-8-x-clients", false);
	private final boolean disableOnBoats = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.disable-on-boats", true);
	private final double spaceBetweenLines = TAB.getInstance().getConfiguration().getConfig().getDouble("scoreboard-teams.unlimited-nametag-mode.space-between-lines", 0.22);
	protected final List<String> disabledUnlimitedWorlds = TAB.getInstance().getConfiguration().getConfig().getStringList("scoreboard-teams.unlimited-nametag-mode.disable-in-worlds", new ArrayList<>());
	private final List<String> dynamicLines = new ArrayList<>(TAB.getInstance().getConfiguration().getConfig().getStringList("scoreboard-teams.unlimited-nametag-mode.dynamic-lines", Arrays.asList(TabConstants.Property.ABOVENAME, TabConstants.Property.NAMETAG, TabConstants.Property.BELOWNAME, "another")));
	private final Map<String, Object> staticLines = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("scoreboard-teams.unlimited-nametag-mode.static-lines");

	//player data by entityId, used for better performance
	private final Map<Integer, TabPlayer> entityIdMap = new ConcurrentHashMap<>();

	//bukkit event listener
	private final EventListener eventListener = new EventListener(this);
	
	private final Set<TabPlayer> playersInDisabledUnlimitedWorlds = Collections.newSetFromMap(new WeakHashMap<>());
	private final String[] disabledUnlimitedWorldsArray = disabledUnlimitedWorlds.toArray(new String[0]);
	private final boolean unlimitedWorldWhitelistMode = disabledUnlimitedWorlds.contains("WHITELIST");
	
	private final Set<TabPlayer> playersDisabledWithAPI = Collections.newSetFromMap(new WeakHashMap<>());
	
	private final VehicleRefresher vehicleManager = new VehicleRefresher(this);

	/**
	 * Constructs new instance with given parameters and loads config options
	 * @param plugin - plugin instance
	 */
	public NameTagX(JavaPlugin plugin) {
		Collections.reverse(dynamicLines);
		Bukkit.getPluginManager().registerEvents(eventListener, plugin);
		TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_PACKET_LISTENER, new PacketListener(this));
		TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_VEHICLE_REFRESHER, vehicleManager);
		TAB.getInstance().debug(String.format("Loaded Unlimited NameTag feature with parameters markerFor18x=%s, disableOnBoats=%s, spaceBetweenLines=%s, disabledUnlimitedWorlds=%s",
				markerFor18x, disableOnBoats, spaceBetweenLines, disabledUnlimitedWorlds));
	}

	@Override
	public void load() {
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			getEntityIdMap().put(((Player) all.getPlayer()).getEntityId(), all);
			updateProperties(all);
			loadArmorStands(all);
			if (isDisabled(all.getWorld())) {
				playersInDisabledUnlimitedWorlds.add(all);
			}
			if (isPlayerDisabled(all)) continue;
			vehicleManager.loadPassengers(all);
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				spawnArmorStands(all, viewer, false);
			}
		}
		super.load();
		startVisibilityRefreshTask();
	}
	
	private void startVisibilityRefreshTask() {
		TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500, "refreshing NameTag visibility", this, TabConstants.CpuUsageCategory.REFRESHING_NAME_TAG_VISIBILITY, () -> {
			
			for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
				if (isPlayerDisabled(p)) continue;
				p.getArmorStandManager().updateVisibility(false);
			}
		});
	}

	@Override
	public void unload() {
		super.unload();
		HandlerList.unregisterAll(eventListener);
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			if (p.getArmorStandManager() != null) p.getArmorStandManager().destroy();
		}
		getEntityIdMap().clear();
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		if (isDisabled(connectedPlayer.getWorld()))
			playersInDisabledUnlimitedWorlds.add(connectedPlayer);
		super.onJoin(connectedPlayer);
		getEntityIdMap().put(((Player) connectedPlayer.getPlayer()).getEntityId(), connectedPlayer);
		loadArmorStands(connectedPlayer);
		if (isPlayerDisabled(connectedPlayer)) return;
		vehicleManager.loadPassengers(connectedPlayer);
		for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
			spawnArmorStands(connectedPlayer, viewer, true);
		}
	}
	
	/**
	 * Returns flat distance between two players ignoring Y value
	 * @param player1 - first player
	 * @param player2 - second player
	 * @return flat distance in blocks
	 */
	private double getDistance(TabPlayer player1, TabPlayer player2) {
		Location loc1 = ((Player) player1.getPlayer()).getLocation();
		Location loc2 = ((Player) player2.getPlayer()).getLocation();
		return Math.sqrt(Math.pow(loc1.getX()-loc2.getX(), 2) + Math.pow(loc1.getZ()-loc2.getZ(), 2));
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		super.onQuit(disconnectedPlayer);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (all.getArmorStandManager() != null) all.getArmorStandManager().unregisterPlayer(disconnectedPlayer);
		}
		entityIdMap.remove(((Player) disconnectedPlayer.getPlayer()).getEntityId());
		if (disconnectedPlayer.getArmorStandManager() != null) { //player was not loaded yet
			disconnectedPlayer.getArmorStandManager().destroy();
			TAB.getInstance().getCPUManager().runTaskLater(500, "processing onQuit", this, TabConstants.CpuUsageCategory.PLAYER_QUIT, () -> disconnectedPlayer.getArmorStandManager().destroy());
		}
	}

	/**
	 * Spawns armor stands of specified owner to viewer and, mutually if set
	 * @param owner - armor stand owner
	 * @param viewer - player to spawn armor stands for
	 * @param sendMutually - if packets should be sent both ways
	 */
	public void spawnArmorStands(TabPlayer owner, TabPlayer viewer, boolean sendMutually) {
		if (owner == viewer) return; //not displaying own armor stands
		if (((Player) viewer.getPlayer()).getWorld() != ((Player) owner.getPlayer()).getWorld()) return; //different world
		if (isPlayerDisabled(owner)) return;
		if (getDistance(viewer, owner) <= 48) {
			if (((Player)viewer.getPlayer()).canSee((Player)owner.getPlayer()) && !owner.isVanished()) owner.getArmorStandManager().spawn(viewer);
			if (sendMutually && viewer.getArmorStandManager() != null && ((Player)owner.getPlayer()).canSee((Player)viewer.getPlayer()) 
					&& !viewer.isVanished()) viewer.getArmorStandManager().spawn(owner);
		}
	}

	/**
	 * Restarts and loads armor stands from config
	 * @param pl - player to load
	 */
	public void loadArmorStands(TabPlayer pl) {
		pl.setArmorStandManager(new ArmorStandManager());
		pl.setProperty(this, TabConstants.Property.NAMETAG, pl.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue() 
				+ pl.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue()
				+ pl.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
		double height = 0;
		for (String line : dynamicLines) {
			Property p = pl.getProperty(line);
			if (p.getCurrentRawValue().length() == 0) continue;
			pl.getArmorStandManager().addArmorStand(line, new BukkitArmorStand(pl, p, height, false));
			height += spaceBetweenLines;
		}
		for (Entry<String, Object> line : staticLines.entrySet()) {
			Property p = pl.getProperty(line.getKey());
			if (p.getCurrentRawValue().length() == 0) continue;
			pl.getArmorStandManager().addArmorStand(line.getKey(), new BukkitArmorStand(pl, p, Double.parseDouble(line.getValue().toString()), true));
		}
		fixArmorStandHeights(pl);
	}

	/**
	 * Fixes heights of all armor stands of specified player due to dynamic lines
	 * @param p - player to fix armor stands heights for
	 */
	public void fixArmorStandHeights(TabPlayer p) {
		double currentY = -spaceBetweenLines;
		for (ArmorStand as : p.getArmorStandManager().getArmorStands()) {
			if (as.hasStaticOffset()) continue;
			if (as.getProperty().get().length() != 0) {
				currentY += spaceBetweenLines;
				as.setOffset(currentY);
			}
		}
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		super.refresh(refreshed, force);
		if (isPlayerDisabled(refreshed)) return;
		if (force) {
			refreshed.getArmorStandManager().destroy();
			loadArmorStands(refreshed);
			vehicleManager.loadPassengers(refreshed);
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				if (viewer == refreshed) continue;
				if (viewer.getWorld().equals(refreshed.getWorld())) {
					refreshed.getArmorStandManager().spawn(viewer);
				}
			}
		} else {
			boolean fix = false;
			for (ArmorStand as : refreshed.getArmorStandManager().getArmorStands()) {
				if (as.getProperty().update()) {
					as.refresh();
					fix = true;
				}
			}
			if (fix) fixArmorStandHeights(refreshed);
		}
	}

	/**
	 * Updates raw values of properties for specified player
	 * @param p - player to update
	 */
	@Override
	public boolean updateProperties(TabPlayer p) {
		boolean changed = super.updateProperties(p);
		if (p.loadPropertyFromConfig(this, TabConstants.Property.CUSTOMTAGNAME, p.getName())) changed = true;
		if (p.setProperty(this, TabConstants.Property.NAMETAG, p.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue() +
				p.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue() + p.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue())) changed = true;
		for (String property : dynamicLines) {
			if (!property.equals(TabConstants.Property.NAMETAG) && p.loadPropertyFromConfig(this, property)) changed = true;
		}
		for (String property : staticLines.keySet()) {
			if (!property.equals(TabConstants.Property.NAMETAG) && p.loadPropertyFromConfig(this, property)) changed = true;
		}
		return changed;
	}

	@Override
	public String getFeatureName() {
		return "Unlimited NameTags";
	}

	@Override
	public boolean getTeamVisibility(TabPlayer p, TabPlayer viewer) {
		if (p.hasInvisibilityPotion()) return false; //1.8.x client sided bug
		if (vehicleManager == null) return false; //class is still being initialized and this was called from super constructor
		return vehicleManager.isOnBoat(p) || isPlayerDisabled(p);
	}

	public Map<Integer, TabPlayer> getEntityIdMap() {
		return entityIdMap;
	}

	public boolean isDisabled(String world) {
		boolean contains = contains(disabledUnlimitedWorldsArray, world);
		if (unlimitedWorldWhitelistMode) contains = !contains;
		return contains;
	}

	public boolean isPlayerDisabled(TabPlayer p) {
		return isDisabledPlayer(p) || playersInDisabledUnlimitedWorlds.contains(p) || hasTeamHandlingPaused(p) || hasDisabledArmorStands(p);
	}

	public boolean isMarkerFor18x() {
		return markerFor18x;
	}

	public Set<TabPlayer> getPlayersInDisabledUnlimitedWorlds() {
		return playersInDisabledUnlimitedWorlds;
	}
	
	public VehicleRefresher getVehicleManager() {
		return vehicleManager;
	}
	
	@Override
	public void pauseTeamHandling(TabPlayer player) {
		if (teamHandlingPaused.contains(player)) return;
		if (!isDisabledPlayer(player)) unregisterTeam(player);
		teamHandlingPaused.add(player); //adding after, so unregisterTeam method runs
		player.getArmorStandManager().destroy();
	}
	
	@Override
	public void resumeTeamHandling(TabPlayer player) {
		if (!teamHandlingPaused.contains(player)) return;
		teamHandlingPaused.remove(player); //removing before, so registerTeam method runs
		if (!isDisabledPlayer(player)) registerTeam(player);
		if (!isPlayerDisabled(player)) {
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				spawnArmorStands(player, viewer, false);
			}
		}
	}

	@Override
	public void disableArmorStands(TabPlayer player) {
		if (playersDisabledWithAPI.contains(player)) return;
		playersDisabledWithAPI.add(player);
		player.getArmorStandManager().destroy();
		updateTeamData(player);
	}

	@Override
	public void enableArmorStands(TabPlayer player) {
		if (!playersDisabledWithAPI.contains(player)) return;
		playersDisabledWithAPI.remove(player);
		if (!isPlayerDisabled(player)) {
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				spawnArmorStands(player, viewer, false);
			}
		}
		updateTeamData(player);
	}

	@Override
	public boolean hasDisabledArmorStands(TabPlayer player) {
		return playersDisabledWithAPI.contains(player);
	}
	
	@Override
	public void setPrefix(TabPlayer player, String prefix) {
		player.getProperty(TabConstants.Property.TAGPREFIX).setTemporaryValue(prefix);
		rebuildNameTagLine(player);
		player.forceRefresh();
	}

	@Override
	public void setSuffix(TabPlayer player, String suffix) {
		player.getProperty(TabConstants.Property.TAGSUFFIX).setTemporaryValue(suffix);
		rebuildNameTagLine(player);
		player.forceRefresh();
	}

	@Override
	public void resetPrefix(TabPlayer player) {
		player.getProperty(TabConstants.Property.TAGPREFIX).setTemporaryValue(null);
		rebuildNameTagLine(player);
		player.forceRefresh();
	}

	@Override
	public void resetSuffix(TabPlayer player) {
		player.getProperty(TabConstants.Property.TAGSUFFIX).setTemporaryValue(null);
		rebuildNameTagLine(player);
		player.forceRefresh();
	}

	@Override
	public void setName(TabPlayer player, String customName) {
		player.getProperty(TabConstants.Property.CUSTOMTAGNAME).setTemporaryValue(customName);
		rebuildNameTagLine(player);
		player.forceRefresh();
	}

	@Override
	public void setLine(TabPlayer player, String line, String value) {
		player.getProperty(line).setTemporaryValue(value);
		player.forceRefresh();
	}

	@Override
	public void resetName(TabPlayer player) {
		player.getProperty(TabConstants.Property.CUSTOMTAGNAME).setTemporaryValue(null);
		rebuildNameTagLine(player);
		player.forceRefresh();
	}

	@Override
	public void resetLine(TabPlayer player, String line) {
		player.getProperty(line).setTemporaryValue(null);
		player.forceRefresh();
	}

	@Override
	public String getCustomName(TabPlayer player) {
		return player.getProperty(TabConstants.Property.CUSTOMTAGNAME).getTemporaryValue();
	}

	@Override
	public String getCustomLineValue(TabPlayer player, String line) {
		return player.getProperty(line).getTemporaryValue();
	}

	@Override
	public String getOriginalName(TabPlayer player) {
		return player.getProperty(TabConstants.Property.CUSTOMTAGNAME).getOriginalRawValue();
	}

	@Override
	public String getOriginalLineValue(TabPlayer player, String line) {
		return player.getProperty(line).getOriginalRawValue();
	}
	
	private void rebuildNameTagLine(TabPlayer player) {
		player.setProperty(this, TabConstants.Property.NAMETAG, player.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue() + 
				player.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue() + player.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
	}

	public boolean isDisableOnBoats() {
		return disableOnBoats;
	}

	@Override
	public List<String> getDefinedLines() {
		List<String> lines = new ArrayList<>(dynamicLines);
		lines.addAll(staticLines.keySet());
		return lines;
	}
}