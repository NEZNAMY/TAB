package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

/**
 * A class representing an armor stand attached to a player (if the feature is enabled)
 */
public class BukkitArmorStand implements ArmorStand {

	private static int idCounter = 2000000000;

	private TabPlayer owner;
	private Player player;
	private double yOffset;
	private int entityId = idCounter++;
	private PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityId);
	private UUID uuid = UUID.randomUUID();
	private boolean sneaking;
	private boolean visible;

	private List<TabPlayer> nearbyPlayers = Collections.synchronizedList(new ArrayList<TabPlayer>());
	private Property property;
	private boolean staticOffset;
	private boolean markerFor18x;

	public BukkitArmorStand(TabPlayer owner, Property property, double yOffset, boolean staticOffset) {
		this.owner = owner;
		this.staticOffset = staticOffset;
		player = (Player) owner.getPlayer();
		this.yOffset = yOffset;
		this.property = property;
		markerFor18x = ((NameTagX)Shared.featureManager.getFeature("nametagx")).markerFor18x;
		refresh();
	}
	
	public void refresh() {
		visible = getVisibility();
		updateMetadata();
	}
	
	public Property getProperty() {
		return property;
	}
	
	public boolean hasStaticOffset() {
		return staticOffset;
	}
	
	public double getOffset() {
		return yOffset;
	}
	
	public void setOffset(double offset) {
		if (yOffset == offset) return;
		yOffset = offset;
		for (TabPlayer all : getNearbyPlayers()) {
			all.sendPacket(new PacketPlayOutEntityTeleport(entityId, getArmorStandLocationFor(all)));
		}
	}
	
	public PacketPlayOut[] getSpawnPackets(TabPlayer viewer, boolean addToRegistered) {
		visible = getVisibility();
		if (!nearbyPlayers.contains(viewer) && addToRegistered) nearbyPlayers.add(viewer);
		DataWatcher dataWatcher = createDataWatcher(property.getFormat(viewer), viewer);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
			return new PacketPlayOut[] {
					new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(viewer), null),
					new PacketPlayOutEntityMetadata(entityId, dataWatcher)
			};
		} else {
			return new PacketPlayOut[] {
					new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(viewer), dataWatcher)
			};
		}
	}
	
	public void spawn(TabPlayer viewer, boolean addToRegistered) {
		for (PacketPlayOut packet : getSpawnPackets(viewer, true)) {
			viewer.sendPacket(packet);
		}
	}
	
	public PacketPlayOutEntityTeleport getTeleportPacket(TabPlayer viewer) {
		return new PacketPlayOutEntityTeleport(entityId, getArmorStandLocationFor(viewer));
	}
	
	private Location getArmorStandLocationFor(TabPlayer viewer) {
		return viewer.getVersion().getMinorVersion() == 8 && !markerFor18x ? getLocation().clone().add(0,-2,0) : getLocation();
	}
	
	public void destroy(TabPlayer viewer) {
		nearbyPlayers.remove(viewer);
		viewer.sendPacket(destroyPacket);
	}
	
	public void teleport() {
		for (TabPlayer all : getNearbyPlayers()) {
			all.sendPacket(getTeleportPacket(all));
		}
	}
	
	public void teleport(TabPlayer viewer) {
		viewer.sendPacket(getTeleportPacket(viewer));
	}
	
	public void sneak(boolean sneaking) {
		if (this.sneaking == sneaking) return; //idk
		this.sneaking = sneaking;
		for (TabPlayer viewer : getNearbyPlayers()) {
			if (viewer.getVersion().getMinorVersion() == 14 && !Configs.SECRET_armorstands_always_visible) {
				//1.14.x client sided bug, despawning completely
				if (sneaking) {
					viewer.sendPacket(destroyPacket);
				} else {
					for (PacketPlayOut packet : getSpawnPackets(viewer, false)) {
						viewer.sendPacket(packet);
					}
				}
			} else {
				//respawning so there's no animation and it's instant
				viewer.sendPacket(destroyPacket);
				Runnable spawn = new Runnable() {

					@Override
					public void run() {
						for (PacketPlayOut packet : getSpawnPackets(viewer, false)) {
							viewer.sendPacket(packet);
						}
					}
				};
				if (viewer.getVersion().getMinorVersion() == 8) {
					//1.8.0 client sided bug
					Shared.cpu.runTaskLater(50, "compensating for 1.8.0 bugs", TabFeature.NAMETAGX, UsageType.v1_8_0_BUG_COMPENSATION, spawn);
				} else {
					spawn.run();
				}
			}
		}
	}
	
	public void destroy() {
		for (TabPlayer all : Shared.getPlayers()) all.sendPacket(destroyPacket);
		nearbyPlayers.clear();
	}
	
	public void updateVisibility() {
		if (getVisibility() != visible) {
			visible = !visible;
			updateMetadata();
		}
	}
	
	private void updateMetadata() {
		for (TabPlayer viewer : getNearbyPlayers()) {
			viewer.sendPacket(new PacketPlayOutEntityMetadata(entityId, createDataWatcher(property.getFormat(viewer), viewer)));
		}
	}
	
	public boolean getVisibility() {
		if (Configs.SECRET_armorstands_always_visible) return true;
		if (((me.neznamy.tab.platforms.bukkit.BukkitTabPlayer)owner).isDisguised()) return false;
		return !player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.getGameMode() != GameMode.SPECTATOR && !owner.hasHiddenNametag() && property.get().length() > 0 && !owner.isOnBoat();
	}
	
	public Location getLocation() {
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
	
	private double getY() {
		//1.14+ bukkit api bug
		Entity vehicle = player.getVehicle();
		if (vehicle != null) {
			if (vehicle.getType() == EntityType.HORSE) {
				return player.getVehicle().getLocation().getY() + 0.85;
			}
			if (vehicle.getType().toString().equals("DONKEY")) {
				return player.getVehicle().getLocation().getY() + 0.525;
			}
			if (vehicle.getType() == EntityType.PIG) {
				return player.getVehicle().getLocation().getY() + 0.325;
			}
			if (vehicle.getType().toString().equals("STRIDER")) { //preventing errors on <1.16
				return player.getVehicle().getLocation().getY() + 1.15;
			}
		}
		return player.getLocation().getY();
	}
	
	public int getEntityId() {
		return entityId;
	}
	
	public void removeFromRegistered(TabPlayer viewer) {
		nearbyPlayers.remove(viewer);
	}
	
	public DataWatcher createDataWatcher(String displayName, TabPlayer viewer) {
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
	
	public List<TabPlayer> getNearbyPlayers(){
		return new ArrayList<TabPlayer>(nearbyPlayers);
	}
	
	private boolean isNameVisiblyEmpty(String displayName) {
		return IChatBaseComponent.fromColoredText(displayName).toRawText().replace(" ", "").length() == 0;
	}
}