package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.potion.PotionEffectType;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing an armor stand attached to a player (if the feature is enabled)
 */
public class BukkitArmorStand implements ArmorStand {

	//entity id counter to pick unique entity IDs
	private static int idCounter = 2000000000;
	
	//nametag feature
	private NameTagX manager;
	
	//owner of the armor stand
	private TabPlayer owner;

	//instance of Bukkit player
	private Player player;

	//offset in blocks, 0 for original height
	private double yOffset;

	//entity ID of this armor stand
	private int entityId = idCounter++;

	//unique ID of this armor stand
	private UUID uuid = UUID.randomUUID();

	//sneaking flag of armor stands
	private boolean sneaking;

	//armor stand visibility
	private boolean visible;

	//property dedicated to this armor stand
	private Property property;

	//if offset is static or dynamic based on other armor stands
	private boolean staticOffset;
	
	//entity destroy packet
	private PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityId);

	/**
	 * Constructs new instance with given parameters
	 * @param owner - armor stand owner
	 * @param property - property for armor stand's name
	 * @param yOffset - offset in blocks
	 * @param staticOffset - if offset is static or not
	 */
	public BukkitArmorStand(TabPlayer owner, Property property, double yOffset, boolean staticOffset) {
		this.manager = (NameTagX) TAB.getInstance().getFeatureManager().getFeature("nametagx");
		this.owner = owner;
		this.staticOffset = staticOffset;
		player = (Player) owner.getPlayer();
		this.yOffset = yOffset;
		this.property = property;
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
		for (TabPlayer all : owner.getArmorStandManager().getNearbyPlayers()) {
			all.sendCustomPacket(getTeleportPacket(all), "UnlimitedNametags - Changing offset");
		}
	}

	/**
	 * Returns list of packets to send to make armor stand spawn with metadata
	 * @param viewer - viewer to apply relational placeholders for
	 * @param addToRegistered - if player should be added to registered or not
	 * @return List of packets that spawn the armor stand
	 */
	public TabPacket[] getSpawnPackets(TabPlayer viewer) {
		visible = getVisibility();
		DataWatcher dataWatcher = createDataWatcher(property.getFormat(viewer), viewer);
		if (NMSStorage.getInstance().getMinorVersion() >= 15) {
			return new TabPacket[] {
					new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(viewer), null),
					new PacketPlayOutEntityMetadata(entityId, dataWatcher)
			};
		} else {
			return new TabPacket[] {
					new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(viewer), dataWatcher),
			};
		}
	}

	@Override
	public void spawn(TabPlayer viewer) {
		for (TabPacket packet : getSpawnPackets(viewer)) {
			viewer.sendCustomPacket(packet, "UnlimitedNametags - Spawning");
		}
	}

	/**
	 * Returns location where armor stand should be for specified viewer
	 * @param viewer - player to get location for
	 * @return location of armor stand
	 */
	public Location getArmorStandLocationFor(TabPlayer viewer) {
		return viewer.getVersion().getMinorVersion() == 8 && !manager.isMarkerFor18x() ? getLocation().clone().add(0,-2,0) : getLocation();
	}

	@Override
	public void destroy(TabPlayer viewer) {
		viewer.sendCustomPacket(destroyPacket, "UnlimitedNametags - Despawning");
	}

	@Override
	public void teleport() {
		for (TabPlayer all : owner.getArmorStandManager().getNearbyPlayers()) {
			all.sendCustomPacket(getTeleportPacket(all), "UnlimitedNametags - Global teleporting");
		}
	}

	@Override
	public void teleport(TabPlayer viewer) {
		if (!owner.getArmorStandManager().isNearby(viewer) && viewer != owner) {
			spawn(viewer);
		} else {
			viewer.sendCustomPacket(getTeleportPacket(viewer), "UnlimitedNametags - Teleporting");
		}
	}

	@Override
	public void sneak(boolean sneaking) {
		if (this.sneaking == sneaking) return; //idk
		this.sneaking = sneaking;
		for (TabPlayer viewer : owner.getArmorStandManager().getNearbyPlayers()) {
			if (viewer.getVersion().getMinorVersion() == 14 && !TAB.getInstance().getConfiguration().isArmorStandsAlwaysVisible()) {
				//1.14.x client sided bug, despawning completely
				if (sneaking) {
					viewer.sendCustomPacket(destroyPacket, "UnlimitedNametags - Sneaking");
				} else {
					spawn(viewer);
				}
			} else {
				//respawning so there's no animation and it's instant
				viewer.sendCustomPacket(destroyPacket, "UnlimitedNametags - Sneaking");
				Runnable spawn = () -> spawn(viewer);
				if (viewer.getVersion().getMinorVersion() == 8) {
					//1.8.0 client sided bug
					TAB.getInstance().getCPUManager().runTaskLater(50, "compensating for 1.8.0 bugs", manager, "Compensating for 1.8.0 bugs", spawn);
				} else {
					spawn.run();
				}
			}
		}
	}

	@Override
	public void destroy() {
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) all.sendCustomPacket(destroyPacket, "UnlimitedNametags - Despawning");
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
	public PacketPlayOutEntityTeleport getTeleportPacket(TabPlayer viewer) {
		return new PacketPlayOutEntityTeleport(entityId, getArmorStandLocationFor(viewer));
	}

	/**
	 * Updates armor stand's metadata
	 */
	public void updateMetadata() {
		for (TabPlayer viewer : owner.getArmorStandManager().getNearbyPlayers()) {
			viewer.sendCustomPacket(new PacketPlayOutEntityMetadata(entityId, createDataWatcher(property.getFormat(viewer), viewer)), "UnlimitedNametags - Updating metadata");
		}
	}

	/**
	 * Returns general visibility rule for everyone with limited info
	 * @return true if should be visible, false if not
	 */
	public boolean getVisibility() {
		if (((BukkitTabPlayer)owner).isDisguised() || manager.getPlayersOnBoats().contains(owner)) return false;
		if (TAB.getInstance().getConfiguration().isArmorStandsAlwaysVisible()) return true;
		return !player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.getGameMode() != GameMode.SPECTATOR && !manager.hasHiddenNametag(owner) && property.get().length() > 0;
	}

	/**
	 * Returns general location where armor stand should be at time of calling
	 * @return Location where armor stand should be for everyone
	 */
	public Location getLocation() {
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

	/**
	 * Creates data watcher with specified display name for viewer
	 * @param displayName - armor stand name
	 * @param viewer - player to apply checks against
	 * @return datawatcher for viewer
	 */
	public DataWatcher createDataWatcher(String displayName, TabPlayer viewer) {
		DataWatcher datawatcher = new DataWatcher();

		byte flag = 32; //invisible
		if (sneaking) flag += (byte)2;
		datawatcher.helper().setEntityFlags(flag);
		datawatcher.helper().setCustomName(displayName, viewer.getVersion());

		boolean visibility;
		if (isNameVisiblyEmpty(displayName) || !((Player) viewer.getPlayer()).canSee(player) || manager.hasHiddenNametag(owner, viewer)) {
			visibility = false;
		} else {
			visibility = visible;
		}
		datawatcher.helper().setCustomNameVisible(visibility);

		if (viewer.getVersion().getMinorVersion() > 8 || manager.isMarkerFor18x()) datawatcher.helper().setArmorStandFlags((byte)16);
		return datawatcher;
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