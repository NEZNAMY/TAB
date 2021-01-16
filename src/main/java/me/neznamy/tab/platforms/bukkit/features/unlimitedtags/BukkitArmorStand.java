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
import org.bukkit.potion.PotionEffectType;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitPacketBuilder;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
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
	public BukkitArmorStand(int entityId, TabPlayer owner, Property property, double yOffset, boolean staticOffset) {
		this.entityId = entityId;
		this.owner = owner;
		this.staticOffset = staticOffset;
		player = (Player) owner.getPlayer();
		this.yOffset = yOffset;
		this.property = property;
		markerFor18x = ((NameTagX)TAB.getInstance().getFeatureManager().getFeature("nametagx")).markerFor18x;
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
			all.sendPacket(getTeleportPacket(all));
		}
	}

	/**
	 * Returns list of packets to send to make armor stand spawn with metadata
	 * @param viewer - viewer to apply relational placeholders for
	 * @param addToRegistered - if player should be added to registered or not
	 * @return List of packets that spawn the armor stand
	 */
	public Object[] getSpawnPackets(TabPlayer viewer, boolean addToRegistered) {
		visible = getVisibility();
		if (addToRegistered) nearbyPlayers.add(viewer);
		DataWatcher dataWatcher = createDataWatcher(property.getFormat(viewer), viewer);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
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
	public void spawn(TabPlayer viewer, boolean addToRegistered) {
		for (Object packet : getSpawnPackets(viewer, addToRegistered)) {
			viewer.sendPacket(packet);
		}
	}

	/**
	 * Returns location where armor stand should be for specified viewer
	 * @param viewer - player to get location for
	 * @return location of armor stand
	 */
	private Location getArmorStandLocationFor(TabPlayer viewer) {
		return viewer.getVersion().getMinorVersion() == 8 && !markerFor18x ? getLocation().clone().add(0,-2,0) : getLocation();
	}

	@Override
	public void destroy(TabPlayer viewer) {
		nearbyPlayers.remove(viewer);
		viewer.sendPacket(getDestroyPacket());
	}

	@Override
	public void teleport() {
		for (TabPlayer all : getNearbyPlayers()) {
			all.sendPacket(getTeleportPacket(all));
		}
	}

	@Override
	public void teleport(TabPlayer viewer) {
		viewer.sendPacket(getTeleportPacket(viewer));
	}

	@Override
	public void sneak(boolean sneaking) {
		if (this.sneaking == sneaking) return; //idk
		this.sneaking = sneaking;
		for (TabPlayer viewer : getNearbyPlayers()) {
			if (viewer.getVersion().getMinorVersion() == 14 && !(boolean) TAB.getInstance().getConfiguration().getSecretOption("unlimited-nametag-prefix-suffix-mode.always-visible", false)) {
				//1.14.x client sided bug, despawning completely
				if (sneaking) {
					viewer.sendPacket(getDestroyPacket());
				} else {
					spawn(viewer, false);
				}
			} else {
				//respawning so there's no animation and it's instant
				viewer.sendPacket(getDestroyPacket());
				Runnable spawn = () -> spawn(viewer, false);
				if (viewer.getVersion().getMinorVersion() == 8) {
					//1.8.0 client sided bug
					TAB.getInstance().getCPUManager().runTaskLater(50, "compensating for 1.8.0 bugs", TabFeature.NAMETAGX, UsageType.v1_8_0_BUG_COMPENSATION, spawn);
				} else {
					spawn.run();
				}
			}
		}
	}

	@Override
	public void destroy() {
		for (TabPlayer all : TAB.getInstance().getPlayers()) all.sendPacket(getDestroyPacket());
		nearbyPlayers.clear();
	}

	@Override
	public void updateVisibility() {
		if (getVisibility() != visible) {
			visible = !visible;
			updateMetadata();
		}
	}
	

	/**
	 * Returns teleport packet for specified viewer
	 * @param viewer - player to get location for
	 * @return teleport packet
	 */
	private Object getTeleportPacket(TabPlayer viewer) {
		try {
			return ((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).buildEntityTeleportPacket(entityId, getArmorStandLocationFor(viewer));
		} catch (Exception e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create PacketPlayOutEntityTeleport", e);
		}
	}
	
	private Object getDestroyPacket() {
		try {
			return ((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).buildEntityDestroyPacket(entityId);
		} catch (Exception e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create PacketPlayOutEntityDestroy", e);
		}
	}
	
	private Object getMetadataPacket(DataWatcher dataWatcher) {
		try {
			return ((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).buildEntityMetadataPacket(entityId, dataWatcher);
		} catch (Exception e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create PacketPlayOutEntityMetadata", e);
		}
	}
	
	private Object getSpawnPacket(Location loc, DataWatcher dataWatcher) {
		try {
			return ((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).buildEntitySpawnPacket(entityId, uuid, EntityType.ARMOR_STAND, loc, dataWatcher);
		} catch (Exception e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create PacketPlayOutSpawnEntityLiving", e);
		}
	}

	/**
	 * Updates armor stand's metadata
	 */
	private void updateMetadata() {
		for (TabPlayer viewer : getNearbyPlayers()) {
			viewer.sendPacket(getMetadataPacket(createDataWatcher(property.getFormat(viewer), viewer)));
		}
	}

	/**
	 * Returns general visibility rule for everyone with limited info
	 * @return true if should be visible, false if not
	 */
	private boolean getVisibility() {
		if (((BukkitTabPlayer)owner).isDisguised() || (((NameTagX)TAB.getInstance().getFeatureManager().getFeature("nametagx")).disableOnBoats && owner.isOnBoat())) return false;
		if ((boolean) TAB.getInstance().getConfiguration().getSecretOption("unlimited-nametag-prefix-suffix-mode.always-visible", false)) return true;
		return !player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.getGameMode() != GameMode.SPECTATOR && !owner.hasHiddenNametag() && property.get().length() > 0 && !owner.isOnBoat();
	}

	/**
	 * Returns general location where armor stand should be at time of calling
	 * @return Location where armor stand should be for everyone
	 */
	private Location getLocation() {
		double x = player.getLocation().getX();
		double y = getY() + yOffset + 2;
		double z = player.getLocation().getZ();
		if (player.isSleeping()) {
			y -= 1.76;
		} else {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
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
	private double getY() {
		//1.14+ bukkit api bug
		Entity vehicle = player.getVehicle();
		if (vehicle != null) {
			if (vehicle.getType() == EntityType.HORSE) {
				return vehicle.getLocation().getY() + 0.85;
			}
			if (vehicle.getType().toString().equals("DONKEY")) {
				return vehicle.getLocation().getY() + 0.525;
			}
			if (vehicle.getType() == EntityType.PIG) {
				return vehicle.getLocation().getY() + 0.325;
			}
			if (vehicle.getType().toString().equals("STRIDER")) { //preventing errors on <1.16
				return vehicle.getLocation().getY() + 1.15;
			}
		}
		//1.13+ swimming
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13 && player.isSwimming()) {
			return player.getLocation().getY()-1.22;
		}
		return player.getLocation().getY();
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
	private DataWatcher createDataWatcher(String displayName, TabPlayer viewer) {
		DataWatcher datawatcher = new DataWatcher();

		byte flag = 32; //invisible
		if (sneaking) flag += (byte)2;
		datawatcher.helper().setEntityFlags(flag);
		datawatcher.helper().setCustomName(displayName, viewer.getVersion());

		boolean visible;
		if (isNameVisiblyEmpty(displayName) || !((Player) viewer.getPlayer()).canSee(player)) {
			visible = false;
		} else {
			visible = this.visible;
		}
		datawatcher.helper().setCustomNameVisible(visible);

		if (viewer.getVersion().getMinorVersion() > 8 || markerFor18x) datawatcher.helper().setArmorStandFlags((byte)16);
		return datawatcher;
	}

	/**
	 * Returns list of players in entity tracking range (48 blocks)
	 * @return
	 */
	private Set<TabPlayer> getNearbyPlayers(){
		return new HashSet<TabPlayer>(nearbyPlayers);
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