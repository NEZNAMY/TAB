package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

public class ArmorStand{

	private ITabPlayer owner;
	private Player player;
	private double yOffset;
	private Object nmsEntity = MethodAPI.getInstance().newEntityArmorStand();
	private int entityId = MethodAPI.getInstance().getEntityId(nmsEntity);
	private UUID uuid = UUID.randomUUID();
	private Location location;
	private boolean sneaking;
	private boolean visible;

	private List<ITabPlayer> nearbyPlayers = Collections.synchronizedList(new ArrayList<ITabPlayer>());
	public Property property;
	private boolean staticOffset;

	public ArmorStand(ITabPlayer owner, Property property, double yOffset, boolean staticOffset) {
		this.owner = owner;
		this.staticOffset = staticOffset;
		player = owner.getBukkitEntity();
		this.yOffset = yOffset;
		this.property = property;
		refresh();
		updateLocation(player.getLocation());
	}
	public void refresh() {
		visible = getVisibility();
		updateMetadata();
	}
	public boolean hasStaticOffset() {
		return staticOffset;
	}
	public void setOffset(double offset) {
		if (yOffset == offset) return;
		yOffset = offset;
		updateLocation(player.getLocation());
		synchronized (nearbyPlayers) {
			for (ITabPlayer all : nearbyPlayers) {
				all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityTeleport(nmsEntity, getArmorStandLocationFor(all)));
			}
		}
	}
	public Object[] getSpawnPackets(ITabPlayer viewer, boolean addToRegistered) {
		visible = getVisibility();
		if (!nearbyPlayers.contains(viewer) && addToRegistered) nearbyPlayers.add(viewer);
		DataWatcher dataWatcher = createDataWatcher(property.getFormat(viewer), viewer);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
			return new Object[] {
					new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(viewer)).toNMSNoEx(),
					MethodAPI.getInstance().newPacketPlayOutEntityMetadata(getEntityId(), dataWatcher.toNMS(), true)
			};
		} else {
			return new Object[] {
					new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(viewer)).setDataWatcher(dataWatcher).toNMSNoEx()
			};
		}
	}
	public Object getTeleportPacket(ITabPlayer viewer) {
		return MethodAPI.getInstance().newPacketPlayOutEntityTeleport(nmsEntity, getArmorStandLocationFor(viewer));
	}
	private Location getArmorStandLocationFor(ITabPlayer viewer) {
		return viewer.getVersion().getMinorVersion() == 8 ? location.clone().add(0,-2,0) : location;
	}
	public void destroy(ITabPlayer viewer) {
		nearbyPlayers.remove(viewer);
		viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
	}
	public void teleport() {
		synchronized (nearbyPlayers) {
			for (ITabPlayer all : nearbyPlayers) {
				Object teleportPacket = getTeleportPacket(all);
				all.sendPacket(teleportPacket);
			}
		}
	}
	public void sneak(boolean sneaking) {
		this.sneaking = sneaking;
		updateLocation(player.getLocation());
		synchronized (nearbyPlayers) {
			for (ITabPlayer viewer : nearbyPlayers) {
				if (viewer.getVersion().getMinorVersion() == 14 && !Configs.SECRET_armorstands_always_visible) {
					//1.14.x client sided bug, despawning completely
					if (sneaking) {
						viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
					} else {
						for (Object packet : getSpawnPackets(viewer, false)) {
							viewer.sendPacket(packet);
						}
					}
				} else {
					//respawning so there's no animation and it's instant
					viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
					for (Object packet : getSpawnPackets(viewer, false)) {
						viewer.sendPacket(packet);
					}
				}
			}
		}
	}
	public void destroy() {
		Object destroyPacket = MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId);
		for (ITabPlayer all : Shared.getPlayers()) all.sendPacket(destroyPacket);
		nearbyPlayers.clear();
	}
	public void updateVisibility() {
		if (getVisibility() != visible) {
			visible = !visible;
			updateMetadata();
		}
	}
	private void updateMetadata() {
		synchronized (nearbyPlayers) {
			for (ITabPlayer viewer : nearbyPlayers) {
				viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, createDataWatcher(property.getFormat(viewer), viewer).toNMS(), true));
			}
		}
	}
	public boolean getVisibility() {
		if (Configs.SECRET_armorstands_always_visible) return true;
		return !owner.hasInvisibility() && player.getGameMode() != GameMode.SPECTATOR && !TABAPI.hasHiddenNametag(owner.getUniqueId()) && property.get().length() > 0;
	}
	public void updateLocation(Location newLocation) {
		double x = newLocation.getX();
		double y = newLocation.getY() + yOffset + 2;
		double z = newLocation.getZ();
		if (player.isSleeping()) {
			y -= 1.76;
		} else {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				y -= (sneaking ? 0.45 : 0.18);
			} else {
				y -= (sneaking ? 0.30 : 0.18);
			}
		}
		location = new Location(null,x,y,z);
	}
	public int getEntityId() {
		return entityId;
	}
	public void removeFromRegistered(ITabPlayer viewer) {
		nearbyPlayers.remove(viewer);
	}
	public DataWatcher createDataWatcher(String displayName, ITabPlayer viewer) {
		DataWatcher datawatcher = new DataWatcher(null);

		byte flag = 0;
		if (sneaking) flag += (byte)2;
		flag += (byte)32;
		DataWatcher.Helper.setEntityFlags(datawatcher, flag);

		if (displayName == null) displayName = "";
		DataWatcher.Helper.setCustomName(datawatcher, displayName, viewer.getVersion());

		boolean visible = (isNameVisiblyEmpty(displayName) || !viewer.getBukkitEntity().canSee(player)) ? false : this.visible;
		DataWatcher.Helper.setCustomNameVisible(datawatcher, visible);

		if (viewer.getVersion().getMinorVersion() > 8) DataWatcher.Helper.setArmorStandFlags(datawatcher, (byte)16);
		return datawatcher;
	}
	public List<ITabPlayer> getNearbyPlayers(){
		return nearbyPlayers;
	}
	private boolean isNameVisiblyEmpty(String displayName) {
		return IChatBaseComponent.fromColoredText(displayName).toRawText().replace(" ", "").length() == 0;
	}
}