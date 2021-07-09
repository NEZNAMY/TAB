package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.potion.PotionEffectType;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitPacketBuilder;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

/**
 * A class representing an armor stand attached to a player (if the feature is enabled)
 */
public class BukkitArmorStand implements ArmorStand {

	//owner of the armor stand
	private TabPlayer owner;

	//instance of Bukkit player
	private Player player;

	//offset in blocks, 0 for original height
	private double yOffset;

	//entity ID of this armor stand
	private int entityId;

	//unique ID of this armor stand
	private UUID uuid = UUID.randomUUID();

	//sneaking flag of armor stands
	private boolean sneaking;

	//armor stand visibility
	private boolean visible;

	//list of players in entity tracking range (48 blocks)
	private Set<TabPlayer> nearbyPlayers = Collections.synchronizedSet(new HashSet<TabPlayer>());

	//property dedicated to this armor stand
	private Property property;

	//if offset is static or dynamic based on other armor stands
	private boolean staticOffset;

	//if marker tag should be used for 1.8.x clients
	private boolean markerFor18x;

	/**
	 * Constructs new instance with given parameters
	 * @param owner - armor stand owner
	 * @param property - property for armor stand's name
	 * @param yOffset - offset in blocks
	 * @param staticOffset - if offset is static or not
	 */
	public BukkitArmorStand(int entityId, TabPlayer owner, Property property, double yOffset, boolean staticOffset, boolean markerFor18x) {
		this.entityId = entityId;
		this.owner = owner;
		this.staticOffset = staticOffset;
		player = (Player) owner.getPlayer();
		this.yOffset = yOffset;
		this.property = property;
		this.markerFor18x = markerFor18x;
		visible = getVisibility();
	}

	@Override
	public void refresh() {
		visible = getVisibility();
		updateMetadata();
	}

	@Override
	public Property getProperty() {
		return property;
	}

	@Override
	public boolean hasStaticOffset() {
		return staticOffset;
	}

	@Override
	public double getOffset() {
		return yOffset;
	}

	@Override
	public void setOffset(double offset) {
		if (yOffset == offset) return;
		yOffset = offset;
		for (TabPlayer all : getNearbyPlayers()) {
			all.sendPacket(getTeleportPacket(all), TabFeature.NAMETAGX);
		}
	}

	/**
	 * Returns list of packets to send to make armor stand spawn with metadata
	 * @param viewer - viewer to apply relational placeholders for
	 * @param addToRegistered - if player should be added to registered or not
	 * @return List of packets that spawn the armor stand
	 */
	public Object[] getSpawnPackets(TabPlayer viewer) {
		visible = getVisibility();
		nearbyPlayers.add(viewer);
		DataWatcher dataWatcher = createDataWatcher(property.getFormat(viewer), viewer);
		if (NMSStorage.getInstance().getMinorVersion() >= 15) {
			return new Object[] {
					getSpawnPacket(getArmorStandLocationFor(viewer), null),
					getMetadataPacket(dataWatcher)
			};
		} else {
			return new Object[] {
					getSpawnPacket(getArmorStandLocationFor(viewer), dataWatcher)
			};
		}
	}

	@Override
	public void spawn(TabPlayer viewer) {
		for (Object packet : getSpawnPackets(viewer)) {
			viewer.sendPacket(packet, TabFeature.NAMETAGX);
		}
	}

	/**
	 * Returns location where armor stand should be for specified viewer
	 * @param viewer - player to get location for
	 * @return location of armor stand
	 */
	protected Location getArmorStandLocationFor(TabPlayer viewer) {
		return viewer.getVersion().getMinorVersion() == 8 && !markerFor18x ? getLocation().clone().add(0,-2,0) : getLocation();
	}

	@Override
	public void destroy(TabPlayer viewer) {
		nearbyPlayers.remove(viewer);
		viewer.sendPacket(getDestroyPacket(), TabFeature.NAMETAGX);
	}

	@Override
	public void teleport() {
		for (TabPlayer all : getNearbyPlayers()) {
			all.sendPacket(getTeleportPacket(all), TabFeature.NAMETAGX);
		}
	}

	@Override
	public void teleport(TabPlayer viewer) {
		viewer.sendPacket(getTeleportPacket(viewer), TabFeature.NAMETAGX);
	}

	@Override
	public void sneak(boolean sneaking) {
		if (this.sneaking == sneaking) return; //idk
		this.sneaking = sneaking;
		for (TabPlayer viewer : getNearbyPlayers()) {
			if (viewer.getVersion().getMinorVersion() == 14 && !TAB.getInstance().getConfiguration().isArmorStandsAlwaysVisible()) {
				//1.14.x client sided bug, despawning completely
				if (sneaking) {
					viewer.sendPacket(getDestroyPacket(), TabFeature.NAMETAGX);
				} else {
					spawn(viewer);
				}
			} else {
				//respawning so there's no animation and it's instant
				viewer.sendPacket(getDestroyPacket(), TabFeature.NAMETAGX);
				Runnable spawn = () -> spawn(viewer);
				if (viewer.getVersion().getMinorVersion() == 8) {
					//1.8.0 client sided bug
					TAB.getInstance().getCPUManager().runTaskLater(50, "compensating for 1.8.0 bugs", TabFeature.NAMETAGX, UsageType.V1_8_0_BUG_COMPENSATION, spawn);
				} else {
					spawn.run();
				}
			}
		}
	}

	@Override
	public void destroy() {
		for (TabPlayer all : TAB.getInstance().getPlayers()) all.sendPacket(getDestroyPacket(), TabFeature.NAMETAGX);
		nearbyPlayers.clear();
	}

	@Override
	public void updateVisibility(boolean force) {
		boolean visibility = getVisibility();
		if (visible != visibility || force) {
			visible = visibility;
			updateMetadata();
		}
	}
	

	/**
	 * Returns teleport packet for specified viewer
	 * @param viewer - player to get location for
	 * @return teleport packet
	 */
	protected Object getTeleportPacket(TabPlayer viewer) {
		try {
			return ((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).buildEntityTeleportPacket(entityId, getArmorStandLocationFor(viewer));
		} catch (Exception e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create PacketPlayOutEntityTeleport", e);
		}
	}
	
	/**
	 * Returns destroy packet
	 * @return destroy packet
	 */
	protected Object getDestroyPacket() {
		try {
			return ((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).buildEntityDestroyPacket(entityId);
		} catch (Exception e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create PacketPlayOutEntityDestroy", e);
		}
	}
	
	/**
	 * Returns metadata packet with specified datawatcher
	 * @param dataWatcher - datawatcher
	 * @return metadata packet
	 */
	protected Object getMetadataPacket(DataWatcher dataWatcher) {
		try {
			return ((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).buildEntityMetadataPacket(entityId, dataWatcher);
		} catch (Exception e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create PacketPlayOutEntityMetadata", e);
		}
	}
	
	/**
	 * Returns spawn packet with specified parameters
	 * @param loc - location to spawn at
	 * @param dataWatcher - datawatcher
	 * @return spawn packet
	 */
	protected Object getSpawnPacket(Location loc, DataWatcher dataWatcher) {
		try {
			return ((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).buildEntitySpawnPacket(entityId, uuid, EntityType.ARMOR_STAND, loc, dataWatcher);
		} catch (Exception e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create PacketPlayOutSpawnEntityLiving", e);
		}
	}

	/**
	 * Updates armor stand's metadata
	 */
	protected void updateMetadata() {
		for (TabPlayer viewer : getNearbyPlayers()) {
			viewer.sendPacket(getMetadataPacket(createDataWatcher(property.getFormat(viewer), viewer)), TabFeature.NAMETAGX);
		}
	}

	/**
	 * Returns general visibility rule for everyone with limited info
	 * @return true if should be visible, false if not
	 */
	protected boolean getVisibility() {
		if (((BukkitTabPlayer)owner).isDisguised() || (((NameTagX)TAB.getInstance().getFeatureManager().getFeature("nametagx")).getPlayersOnBoats().contains(owner))) return false;
		if (TAB.getInstance().getConfiguration().isArmorStandsAlwaysVisible()) return true;
		return !player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.getGameMode() != GameMode.SPECTATOR && !owner.hasHiddenNametag() && property.get().length() > 0;
	}

	/**
	 * Returns general location where armor stand should be at time of calling
	 * @return Location where armor stand should be for everyone
	 */
	protected Location getLocation() {
		double x = player.getLocation().getX();
		double y = getY() + yOffset + 2;
		double z = player.getLocation().getZ();
		if (player.isSleeping()) {
			y -= 1.76;
		} else {
			if (NMSStorage.getInstance().getMinorVersion() >= 9) {
				y -= (sneaking ? 0.45 : 0.18);
			} else {
				y -= (sneaking ? 0.30 : 0.18);
			}
		}
		return new Location(null,x,y,z);
	}

	/**
	 * Returns Y where player is based on player's vehicle due to bukkit API bug
	 * @return correct player's Y
	 */
	protected double getY() {
		//1.14+ server sided bug
		Entity vehicle = player.getVehicle();
		if (vehicle != null) {
			if (vehicle.getType().toString().contains("HORSE")) { //covering all 3 horse types
				return vehicle.getLocation().getY() + 0.85;
			}
			if (vehicle.getType().toString().equals("DONKEY")) { //1.11+
				return vehicle.getLocation().getY() + 0.525;
			}
			if (vehicle.getType() == EntityType.PIG) {
				return vehicle.getLocation().getY() + 0.325;
			}
			if (vehicle.getType().toString().equals("STRIDER")) { //1.16+
				return vehicle.getLocation().getY() + 1.15;
			}
		}
        //1.13+ swimming or 1.9+ flying with elytra
        if (isSwimming() || (NMSStorage.getInstance().getMinorVersion() >= 9 && player.isGliding())) {
            return player.getLocation().getY()-1.22;
        }
		return player.getLocation().getY();
	}
	
	private boolean isSwimming() {
		if (NMSStorage.getInstance().getMinorVersion() >= 14 && player.getPose() == Pose.SWIMMING) return true;
		return NMSStorage.getInstance().getMinorVersion() == 13 && player.isSwimming();
	}

	@Override
	public int getEntityId() {
		return entityId;
	}

	@Override
	public void removeFromRegistered(TabPlayer viewer) {
		nearbyPlayers.remove(viewer);
	}

	/**
	 * Creates data watcher with specified display name for viewer
	 * @param displayName - armor stand name
	 * @param viewer - player to apply checks against
	 * @return datawatcher for viewer
	 */
	protected DataWatcher createDataWatcher(String displayName, TabPlayer viewer) {
		DataWatcher datawatcher = new DataWatcher();

		byte flag = 32; //invisible
		if (sneaking) flag += (byte)2;
		datawatcher.helper().setEntityFlags(flag);
		datawatcher.helper().setCustomName(displayName, viewer.getVersion());

		boolean visibility;
		if (isNameVisiblyEmpty(displayName) || !((Player) viewer.getPlayer()).canSee(player) || owner.hasHiddenNametag(viewer.getUniqueId())) {
			visibility = false;
		} else {
			visibility = visible;
		}
		datawatcher.helper().setCustomNameVisible(visibility);

		if (viewer.getVersion().getMinorVersion() > 8 || markerFor18x) datawatcher.helper().setArmorStandFlags((byte)16);
		return datawatcher;
	}

	@Override
	public Set<TabPlayer> getNearbyPlayers(){
		synchronized (nearbyPlayers) {
			return new HashSet<>(nearbyPlayers);
		}
	}

	/**
	 * Returns true if display name is in fact empty, for example only containing color codes
	 * @param displayName - string to check
	 * @return true if it's empty, false if not
	 */
	private boolean isNameVisiblyEmpty(String displayName) {
		return IChatBaseComponent.fromColoredText(displayName).toRawText().replace(" ", "").length() == 0;
	}
}