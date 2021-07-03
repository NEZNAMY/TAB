package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.NameTag;

/**
 * The core class for unlimited nametag mode
 */
public class NameTagX extends NameTag {

	//config options
	private boolean markerFor18x;
	private boolean disableOnBoats;
	private double spaceBetweenLines;
	
	//list of worlds with unlimited nametag mode disabled
	protected List<String> disabledUnlimitedWorlds;
	
	//list of defined dynamic lines
	private List<String> dynamicLines = Arrays.asList(PropertyUtils.BELOWNAME, PropertyUtils.NAMETAG, PropertyUtils.ABOVENAME);
	
	//map of defined static lines
	private Map<String, Object> staticLines = new ConcurrentHashMap<>();

	//entity id counter to pick unique entity IDs
	private int idCounter = 2000000000;

	//player data by entityId, used for better performance
	private Map<Integer, TabPlayer> entityIdMap = new ConcurrentHashMap<>();
	
	//map of vehicles carrying players
	private Map<Integer, List<Entity>> vehicles = new ConcurrentHashMap<>();
	
	//bukkit event listener
	private EventListener eventListener;
	
	//list of players currently on boats
	private List<TabPlayer> playersOnBoats = new ArrayList<>();
	
	//list of players currently in a vehicle
	private Map<TabPlayer, Entity> playersInVehicle = new ConcurrentHashMap<>();
	
	private Map<TabPlayer, Location> playerLocations = new ConcurrentHashMap<>();
	
	private Set<TabPlayer> playersInDisabledUnlimitedWorlds = new HashSet<>();

	/**
	 * Constructs new instance with given parameters and loads config options
	 * @param plugin - plugin instance
	 * @param nms - nms storage
	 * @param tab - tab instance
	 */
	public NameTagX(JavaPlugin plugin, NMSStorage nms, TAB tab) {
		super(tab);
		markerFor18x = tab.getConfiguration().getConfig().getBoolean("unlimited-nametag-prefix-suffix-mode.use-marker-tag-for-1-8-x-clients", false);
		disableOnBoats = tab.getConfiguration().getConfig().getBoolean("unlimited-nametag-prefix-suffix-mode.disable-on-boats", true);
		spaceBetweenLines = tab.getConfiguration().getConfig().getDouble("unlimited-nametag-prefix-suffix-mode.space-between-lines", 0.22);
		disabledUnlimitedWorlds = tab.getConfiguration().getConfig().getStringList("disable-features-in-worlds.unlimited-nametags", Arrays.asList("disabledworld"));
		if (tab.getConfiguration().getPremiumConfig() != null) {
			List<String> realList = tab.getConfiguration().getPremiumConfig().getStringList("unlimited-nametag-mode-dynamic-lines", Arrays.asList(PropertyUtils.ABOVENAME, PropertyUtils.NAMETAG, PropertyUtils.BELOWNAME, "another"));
			dynamicLines = new ArrayList<>();
			dynamicLines.addAll(realList);
			Collections.reverse(dynamicLines);
			staticLines = tab.getConfiguration().getPremiumConfig().getConfigurationSection("unlimited-nametag-mode-static-lines");
		}
		refreshUsedPlaceholders();
		eventListener = new EventListener(this);
		Bukkit.getPluginManager().registerEvents(eventListener, plugin);
		tab.getFeatureManager().registerFeature("nametagx-packet", new PacketListener(this, nms, tab));
		tab.debug(String.format("Loaded Unlimited nametag feature with parameters markerFor18x=%s, disableOnBoats=%s, spaceBetweenLines=%s, disabledUnlimitedWorlds=%s",
				markerFor18x, disableOnBoats, spaceBetweenLines, disabledUnlimitedWorlds));
	}

	@Override
	public void load() {
		super.load();
		for (TabPlayer all : tab.getPlayers()) {
			getEntityIdMap().put(((Player) all.getPlayer()).getEntityId(), all);
			loadArmorStands(all);
			if (isDisabledWorld(disabledUnlimitedWorlds, all.getWorldName())) {
				playersInDisabledUnlimitedWorlds.add(all);
			}
			if (isInDisabledWorld(all)) continue;
			loadPassengers(all);
			for (TabPlayer viewer : tab.getPlayers()) {
				spawnArmorStands(all, viewer, false);
			}
		}
		startVisibilityRefreshTask();
		startVehicleTickingTask();
	}
	
	private void startVisibilityRefreshTask() {
		tab.getCPUManager().startRepeatingMeasuredTask(500, "refreshing nametag visibility", getFeatureType(), UsageType.REFRESHING_NAMETAG_VISIBILITY_AND_COLLISION, () -> {
			
			for (TabPlayer p : tab.getPlayers()) {
				if (!p.isLoaded() || isInDisabledWorld(p)) continue;
				p.getArmorStandManager().updateVisibility(false);
				if (disableOnBoats) processBoats(p);
				
			}
		});
	}
	
	private void processBoats(TabPlayer p) {
		boolean onBoat = ((Player)p.getPlayer()).getVehicle() != null && ((Player)p.getPlayer()).getVehicle().getType() == EntityType.BOAT;
		if (onBoat) {
			if (!getPlayersOnBoats().contains(p)) {
				getPlayersOnBoats().add(p);
				updateTeamData(p);
			}
		} else {
			if (getPlayersOnBoats().contains(p)) {
				getPlayersOnBoats().remove(p);
				updateTeamData(p);
			}
		}
	}
	
	private void startVehicleTickingTask() {
		tab.getCPUManager().startRepeatingMeasuredTask(100, "ticking vehicles", TabFeature.NAMETAGX, UsageType.TICKING_VEHICLES, () -> {
			
			for (TabPlayer p : TAB.getInstance().getPlayers()) {
				if (!p.isLoaded()) continue;
				if (isInDisabledWorld(p)) {
					playersInVehicle.remove(p);
					playerLocations.remove(p);
				} else {
					processVehicles(p);
					if (!playerLocations.containsKey(p) || !playerLocations.get(p).equals(((Player)p.getPlayer()).getLocation())) {
						playerLocations.put(p, ((Player)p.getPlayer()).getLocation());
						processPassengers((Entity) p.getPlayer());
						//also updating position if player is previewing since we're here as the code would be same if we want to avoid listening to move event
						if (p.isPreviewingNametag() && p.getArmorStandManager() != null) {
							p.getArmorStandManager().teleport(p);
						}
					}
				}
			}
		});
	}
	
	/**
	 * Checks for vehicle changes of player and sends packets if needed
	 * @param p - player to check
	 */
	private void processVehicles(TabPlayer p) {
		Entity vehicle = ((Player)p.getPlayer()).getVehicle();
		if (playersInVehicle.containsKey(p) && vehicle == null) {
			//vehicle exit
			getVehicles().remove(playersInVehicle.get(p).getEntityId());
			p.getArmorStandManager().teleport();
			playersInVehicle.remove(p);
		}
		if (!playersInVehicle.containsKey(p) && vehicle != null) {
			//vehicle enter
			getVehicles().put(vehicle.getEntityId(), getPassengers(vehicle));
			p.getArmorStandManager().teleport();
			playersInVehicle.put(p, vehicle);
		}
	}
	
	/**
	 * Teleports armor stands of all passengers on specified vehicle
	 * @param vehicle - entity to check passengers of
	 */
	private void processPassengers(Entity vehicle) {
		for (Entity passenger : getPassengers(vehicle)) {
			if (passenger instanceof Player) {
				TabPlayer pl = TAB.getInstance().getPlayer(passenger.getUniqueId());
				pl.getArmorStandManager().teleport();
			} else {
				processPassengers(passenger);
			}
		}
	}

	@Override
	public void unload() {
		super.unload();
		HandlerList.unregisterAll(eventListener);
		for (TabPlayer p : tab.getPlayers()) {
			if (p.getArmorStandManager() != null) p.getArmorStandManager().destroy();
		}
		getEntityIdMap().clear();
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		super.onJoin(connectedPlayer);
		getEntityIdMap().put(((Player) connectedPlayer.getPlayer()).getEntityId(), connectedPlayer);
		loadArmorStands(connectedPlayer);
		if (isDisabledWorld(disabledUnlimitedWorlds, connectedPlayer.getWorldName())) {
			playersInDisabledUnlimitedWorlds.add(connectedPlayer);
			return;
		}
		if (isInDisabledWorld(connectedPlayer)) return;
		loadPassengers(connectedPlayer);
		for (TabPlayer viewer : tab.getPlayers()) {
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

	/**
	 * Loads all passengers riding this player and adds them to vehicle list
	 * @param p - player to load passengers of
	 */
	private void loadPassengers(TabPlayer p) {
		if (((Entity) p.getPlayer()).getVehicle() == null) return;
		Entity vehicle = ((Entity) p.getPlayer()).getVehicle();
		getVehicles().put(vehicle.getEntityId(), getPassengers(vehicle));
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		super.onQuit(disconnectedPlayer);
		for (TabPlayer all : tab.getPlayers()) {
			if (all.getArmorStandManager() != null) all.getArmorStandManager().unregisterPlayer(disconnectedPlayer);
		}
		getEntityIdMap().remove(((Player) disconnectedPlayer.getPlayer()).getEntityId());
		playersInVehicle.remove(disconnectedPlayer);
		playerLocations.remove(disconnectedPlayer);
		playersInDisabledUnlimitedWorlds.remove(disconnectedPlayer);
		tab.getCPUManager().runTaskLater(100, "processing onQuit", getFeatureType(), UsageType.PLAYER_QUIT_EVENT, () -> disconnectedPlayer.getArmorStandManager().destroy());
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		super.onWorldChange(p, from, to);
		if (isDisabledWorld(disabledWorlds, p.getWorldName())) {
			playersInDisabledWorlds.add(p);
		} else {
			playersInDisabledWorlds.remove(p);
		}
		Set<TabPlayer> nearby = p.getArmorStandManager().getNearbyPlayers();
		p.getArmorStandManager().destroy();
		loadArmorStands(p);
		loadPassengers(p);
		for (TabPlayer viewer : tab.getPlayers()) {
			viewer.getArmorStandManager().destroy(p);
			if (nearby.contains(viewer) && to.equals(viewer.getWorldName())) spawnArmorStands(p, viewer, true);
		}
	}
	
	/**
	 * Spawns armor stands of specified owner to viewer and, mutually if set
	 * @param owner - armor stand owner
	 * @param viewer - player to spawn armor stands for
	 * @param sendMutually - if packets should be sent both ways
	 */
	private void spawnArmorStands(TabPlayer owner, TabPlayer viewer, boolean sendMutually) {
		if (owner == viewer) return; //not displaying own armorstands
		if (((Player) viewer.getPlayer()).getWorld() != ((Player) owner.getPlayer()).getWorld()) return; //different world
		if (isInDisabledWorld(owner)) return;
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
		pl.setProperty(PropertyUtils.NAMETAG, pl.getProperty(PropertyUtils.TAGPREFIX).getCurrentRawValue() + pl.getProperty(PropertyUtils.CUSTOMTAGNAME).getCurrentRawValue() + pl.getProperty(PropertyUtils.TAGSUFFIX).getCurrentRawValue());
		double height = 0;
		for (String line : dynamicLines) {
			Property p = pl.getProperty(line);
			if (p.getCurrentRawValue().length() == 0) continue;
			pl.getArmorStandManager().addArmorStand(line, new BukkitArmorStand(idCounter++, pl, p, height, false, markerFor18x));
			height += spaceBetweenLines;
		}
		for (Entry<String, Object> line : staticLines.entrySet()) {
			Property p = pl.getProperty(line.getKey());
			if (p.getCurrentRawValue().length() == 0) continue;
			pl.getArmorStandManager().addArmorStand(line.getKey(), new BukkitArmorStand(idCounter++, pl, p, Double.parseDouble(line.getValue().toString()), true, markerFor18x));
		}
		fixArmorStandHeights(pl);
	}

	/**
	 * Fixes heights of all armor stands of specified player due to dynamic lines
	 * @param p - player to fix armor stands heights for
	 */
	public void fixArmorStandHeights(TabPlayer p) {
		p.getArmorStandManager().refresh();
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
		if (isInDisabledWorld(refreshed)) return;
		if (force) {
			refreshed.getArmorStandManager().destroy();
			loadArmorStands(refreshed);
			loadPassengers(refreshed);
			for (TabPlayer viewer : tab.getPlayers()) {
				if (viewer == refreshed) continue;
				if (viewer.getWorldName().equals(refreshed.getWorldName())) {
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
	public void updateProperties(TabPlayer p) {
		super.updateProperties(p);
		p.loadPropertyFromConfig(PropertyUtils.CUSTOMTAGNAME, p.getName());
		p.setProperty(PropertyUtils.NAMETAG, p.getProperty(PropertyUtils.TAGPREFIX).getCurrentRawValue() + p.getProperty(PropertyUtils.CUSTOMTAGNAME).getCurrentRawValue() + p.getProperty(PropertyUtils.TAGSUFFIX).getCurrentRawValue());
		for (String property : dynamicLines) {
			if (!property.equals(PropertyUtils.NAMETAG)) p.loadPropertyFromConfig(property);
		}
		for (String property : staticLines.keySet()) {
			if (!property.equals(PropertyUtils.NAMETAG)) p.loadPropertyFromConfig(property);
		}
	}

	/**
	 * Returns list of all passengers on specified vehicle
	 * @param vehicle - vehicle to check passengers of
	 * @return list of passengers
	 */
	@SuppressWarnings("deprecation")
	public List<Entity> getPassengers(Entity vehicle){
		if (NMSStorage.getInstance().getMinorVersion() >= 11) {
			return vehicle.getPassengers();
		} else {
			if (vehicle.getPassenger() != null) {
				return Arrays.asList(vehicle.getPassenger());
			} else {
				return new ArrayList<>();
			}
		}
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = new HashSet<>(tab.getConfiguration().getConfig().getUsedPlaceholderIdentifiersRecursive(PropertyUtils.TAGPREFIX, PropertyUtils.CUSTOMTAGNAME, PropertyUtils.TAGSUFFIX));
		for (String line : dynamicLines) {
			usedPlaceholders.addAll(tab.getConfiguration().getConfig().getUsedPlaceholderIdentifiersRecursive(line));
		}
		for (String line : staticLines.keySet()) {
			usedPlaceholders.addAll(tab.getConfiguration().getConfig().getUsedPlaceholderIdentifiersRecursive(line));
		}
		for (TabPlayer p : tab.getPlayers()) {
			usedPlaceholders.addAll(tab.getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(p.getProperty(PropertyUtils.TAGPREFIX).getCurrentRawValue(), 
					p.getProperty(PropertyUtils.CUSTOMTAGNAME).getCurrentRawValue(), p.getProperty(PropertyUtils.TAGSUFFIX).getCurrentRawValue(),
					p.getProperty(PropertyUtils.ABOVENAME).getCurrentRawValue(), p.getProperty(PropertyUtils.BELOWNAME).getCurrentRawValue()));
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.NAMETAGX;
	}

	@Override
	public boolean getTeamVisibility(TabPlayer p, TabPlayer viewer) {
		//only visible if player is on boat & config option is enabled and player is not invisible (1.8 bug) or feature is disabled
		return (getPlayersOnBoats().contains(p) && !invisiblePlayers.contains(p.getName())) || isInDisabledWorld(p);
	}

	public Map<Integer, TabPlayer> getEntityIdMap() {
		return entityIdMap;
	}

	public Map<Integer, List<Entity>> getVehicles() {
		return vehicles;
	}

	public List<TabPlayer> getPlayersOnBoats() {
		return playersOnBoats;
	}
	
	public boolean isInDisabledWorld(TabPlayer p) {
		return playersInDisabledWorlds.contains(p) || isDisabledWorld(disabledUnlimitedWorlds, p.getWorldName());
	}
}