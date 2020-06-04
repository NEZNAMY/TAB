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
import me.neznamy.tab.shared.PluginHooks;
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
		visible = getVisibility();
		refreshName();
		updateLocation(player.getLocation());
	}
	public void refreshName() {
		if (property.isUpdateNeeded()) {
			visible = getVisibility();
			updateMetadata();
		}
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
	public PacketPlayOutSpawnEntityLiving getSpawnPacket(ITabPlayer viewer, boolean addToRegistered) {
		visible = getVisibility();
		String displayName = property.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(viewer, owner, property.get()) : property.get();
		if (!nearbyPlayers.contains(viewer) && addToRegistered) nearbyPlayers.add(viewer);
		return new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(viewer)).setDataWatcher(createDataWatcher(displayName, viewer));
	}
	public Object getNMSTeleportPacket(ITabPlayer viewer) {
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
				Object teleportPacket = getNMSTeleportPacket(all);
				all.sendPacket(teleportPacket);
			}
		}
	}
	public void sneak(boolean sneaking) {
		this.sneaking = sneaking;
		updateLocation(player.getLocation());
		synchronized (nearbyPlayers) {
			for (ITabPlayer viewer : nearbyPlayers) {
				String displayName = property.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(viewer, owner, property.get()) : property.get();
				if (viewer.getVersion().getMinorVersion() == 14 && !Configs.SECRET_armorstands_always_visible) {
					//1.14.x client sided bug, despawning completely
					if (sneaking) {
						viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
					} else {
						viewer.sendCustomBukkitPacket(getSpawnPacket(viewer, false));
						if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
							viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(getEntityId(), createDataWatcher(displayName, viewer).toNMS(), true));
						}
					}
				} else {
					//respawning so there's no animation and it's instant
					viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
					viewer.sendCustomBukkitPacket(getSpawnPacket(viewer, false));
					if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
						viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(getEntityId(), createDataWatcher(displayName, viewer).toNMS(), true));
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
			String displayName = property.get();
			for (ITabPlayer viewer : nearbyPlayers) {
				String currentName;
				if (property.hasRelationalPlaceholders()) {
					currentName = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(viewer, owner, displayName);
				} else {
					currentName = displayName;
				}
				viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, createDataWatcher(currentName, viewer).toNMS(), true));
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
	public DataWatcher createDataWatcher(String name, ITabPlayer viewer) {
		byte flag = 0;
		if (sneaking) flag += (byte)2;
		flag += (byte)32;
		DataWatcher datawatcher = new DataWatcher(null);
		if (name == null) name = "";
		DataWatcher.Helper.setEntityFlags(datawatcher, flag);
		DataWatcher.Helper.setCustomName(datawatcher, name);
		boolean visible = (isNameVisiblyEmpty(name) || !viewer.getBukkitEntity().canSee(player)) ? false : this.visible;
		DataWatcher.Helper.setCustomNameVisible(datawatcher, visible);
		if (viewer.getVersion().getMinorVersion() > 8) DataWatcher.Helper.setArmorStandFlags(datawatcher, (byte)16);
		return datawatcher;
	}
	public List<ITabPlayer> getNearbyPlayers(){
		return nearbyPlayers;
	}
	public static boolean isNameVisiblyEmpty(String displayName) {
		return IChatBaseComponent.fromColoredText(displayName).toRawText().replace(" ", "").length() == 0;
	}
}