package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcherSerializer;
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
	
	public ArmorStand(ITabPlayer owner, String format, double yOffset, String ID, boolean staticOffset) {
		this.owner = owner;
		this.staticOffset = staticOffset;
		player = ((TabPlayer)owner).player;
		this.yOffset = yOffset;
		owner.setProperty(ID, format);
		property = owner.properties.get(ID);
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
	public PacketPlayOutSpawnEntityLiving getSpawnPacket(ITabPlayer to, boolean addToRegistered) {
		visible = getVisibility();
		String displayName = property.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, to, property.get()) : property.get();
		if (!nearbyPlayers.contains(to) && addToRegistered) nearbyPlayers.add(to);
		return new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(to)).setDataWatcher(createDataWatcher(displayName, to));
	}
	public Object getNMSTeleportPacket(ITabPlayer to) {
		return MethodAPI.getInstance().newPacketPlayOutEntityTeleport(nmsEntity, getArmorStandLocationFor(to));
	}
	private Location getArmorStandLocationFor(ITabPlayer to) {
		return to.getVersion().getMinorVersion() == 8 ? location.clone().add(0,-2,0) : location;
	}
	public void destroy(ITabPlayer to) {
		nearbyPlayers.remove(to);
		to.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
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
		synchronized (nearbyPlayers) {
			for (ITabPlayer all : nearbyPlayers) {
				String displayName = property.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, all, property.get()) : property.get();
				if (all.getVersion().getMinorVersion() >= 14 && !Configs.SECRET_armorstands_always_visible) {
					//sneaking feature was removed in 1.14, so despawning completely now
					if (sneaking) {
						all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
					} else {
						all.sendCustomBukkitPacket(getSpawnPacket(all, false));
						if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
							all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(getEntityId(), createDataWatcher(displayName, all).toNMS(), true));
						}
					}
				} else {
					//respawning so there's no animation and it's instant
					all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
					all.sendCustomBukkitPacket(getSpawnPacket(all, false));
					if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
						all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(getEntityId(), createDataWatcher(displayName, all).toNMS(), true));
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
			for (ITabPlayer all : nearbyPlayers) {
				String currentName;
				if (property.hasRelationalPlaceholders()) {
					currentName = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, all, displayName);
				} else {
					currentName = displayName;
				}
				all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, createDataWatcher(currentName, all).toNMS(), true));
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
	public void removeFromRegistered(ITabPlayer removed) {
		nearbyPlayers.remove(removed);
	}
	public DataWatcher createDataWatcher(String name, ITabPlayer other) {
		byte flag = 0;
		if (sneaking) flag += (byte)2;
		flag += (byte)32;
		DataWatcher datawatcher = new DataWatcher(null);
		if (name == null) name = "";
		datawatcher.setValue(new DataWatcherObject(0, DataWatcherSerializer.Byte), flag);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.Optional_IChatBaseComponent), Optional.ofNullable(MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(name).toString())));
		} else {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.String), name);
		}
		boolean visible = (name.length() == 0 || !((TabPlayer)other).player.canSee(player)) ? false : this.visible;
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Boolean), visible);
		} else {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Byte), (byte)(visible?1:0));
		}
		if (other.getVersion().getMinorVersion() > 8) datawatcher.setValue(new DataWatcherObject(ProtocolVersion.SERVER_VERSION.getMarkerPosition(), DataWatcherSerializer.Byte), (byte)16);
		return datawatcher;
	}
	public List<ITabPlayer> getNearbyPlayers(){
		return nearbyPlayers;
	}
}