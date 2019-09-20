package me.neznamy.tab.platforms.bukkit.unlimitedtags;

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
import me.neznamy.tab.platforms.bukkit.packets.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcherSerializer;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Placeholders;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public class ArmorStand{

	private ITabPlayer owner;
	private Player player;
	private double yOffset;
	private String ID;
	private Object nmsEntity;

	private int entityId;
	private UUID uuid = UUID.randomUUID();
	private Location location;
	private boolean sneaking;
	private boolean visible;

	private List<ITabPlayer> registeredTo = Collections.synchronizedList(new ArrayList<ITabPlayer>());
	private Property property;

	private long lastLocationRefresh = 0;

	public ArmorStand(ITabPlayer owner, String format, double yOffset, String ID) {
		this.owner = owner;
		player = ((TabPlayer)owner).player;
		this.ID = ID;
		this.yOffset = yOffset;
		nmsEntity = MethodAPI.getInstance().newEntityArmorStand();
		entityId = MethodAPI.getInstance().getEntityId(nmsEntity);
		owner.setProperty(ID, format);
		property = owner.properties.get(ID);
		refreshName();
		updateLocation();
	}
	public String getID() {
		return ID;
	}
	public void setNameFormat(String format) {
		owner.setProperty(ID, format);
	}
	public void refreshName() {
		if (property.isUpdateNeeded()) updateMetadata();
	}
	public PacketPlayOutSpawnEntityLiving getSpawnPacket(ITabPlayer to, boolean addToRegistered) {
		updateLocation();
		String name = property.get();
		name = Placeholders.setRelational(owner, to, name);
		if (!registeredTo.contains(to) && addToRegistered) registeredTo.add(to);
		return new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.valueOf("ARMOR_STAND"), location).setDataWatcher(createDataWatcher(name));
	}
	public Object getNMSTeleportPacket() {
		updateLocation();
		return MethodAPI.getInstance().newPacketPlayOutEntityTeleport(nmsEntity, location);
	}
	public Object getNMSDestroyPacket(ITabPlayer to) {
		registeredTo.remove(to);
		return MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId);
	}
	public void teleport() {
		Object teleportPacket = getNMSTeleportPacket();
		synchronized (registeredTo) {
			for (ITabPlayer all : registeredTo) all.sendPacket(teleportPacket);
		}
	}
	public void sneak(boolean sneaking) {
		this.sneaking = sneaking;
		updateLocation();
		synchronized (registeredTo) {
			for (ITabPlayer all : registeredTo) {
				if (all == owner) continue; //should never be anyway
				all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
				if (sneaking && all.getVersion().getNetworkId() >= ProtocolVersion.v1_14.getNetworkId()) continue; //not spawning for 1.14+ players
				all.sendCustomPacket(getSpawnPacket(all, false));
			}
		}
	}
	public void destroy() {
		Object destroyPacket = MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId);
		synchronized (registeredTo) {
			for (ITabPlayer all : registeredTo) all.sendPacket(destroyPacket);
		}
		registeredTo.clear();
	}
	public void updateVisibility() {
		if (getVisibility() != visible) {
			visible = !visible;
			updateMetadata();
		}
	}
	private void updateMetadata() {
		synchronized (registeredTo) {
			String name = property.get();
			if (property.hasRelationalPlaceholders()) {
				for (ITabPlayer all : registeredTo) {
					name = Placeholders.setRelational(owner, all, name);
					all.sendPacket(new PacketPlayOutEntityMetadata(entityId, createDataWatcher(name), true).toNMS(null));
				}
			} else {
				Object packet = new PacketPlayOutEntityMetadata(entityId, createDataWatcher(name), true).toNMS(null);
				for (ITabPlayer all : registeredTo) all.sendPacket(packet);
			}
		}
	}
	public boolean getVisibility() {
		return !owner.hasInvisibility() && player.getGameMode() != GameMode.valueOf("SPECTATOR") && !TABAPI.hasHiddenNametag(player.getUniqueId()) && property.get().length() > 0;
	}
	private void updateLocation() {
		if (System.currentTimeMillis() - lastLocationRefresh < 50) return;
		lastLocationRefresh = System.currentTimeMillis();
		double x = player.getLocation().getX();
		double y;
		double z = player.getLocation().getZ();
		if (player.isSleeping()) {
			y = player.getLocation().getY() + yOffset - 1.76;
		} else {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				y = player.getLocation().getY() + yOffset - (sneaking ? 0.45 : 0.18);
			} else {
				y = player.getLocation().getY() + yOffset - (sneaking ? 0.30 : 0.18);
			}
		}
		y += 2;
		location = new Location(null,x,y,z);
	}
	public int getEntityId() {
		return entityId;
	}
	public void removeFromRegistered(ITabPlayer removed) {
		registeredTo.remove(removed);
	}
	public DataWatcher createDataWatcher(String name) {
		byte flag = 0;
		if (sneaking) flag += (byte)2;
		flag += (byte)32;
		DataWatcher datawatcher = new DataWatcher(null);
		if (name == null || name.length() == 0) name = "§r";
		datawatcher.setValue(new DataWatcherObject(0, DataWatcherSerializer.Byte), flag);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.Optional_IChatBaseComponent), Optional.ofNullable(Shared.mainClass.createComponent(name)));
		} else {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.String), name);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Boolean), visible);
		} else {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Byte), (byte)(visible?1:0));
		}
		datawatcher.setValue(new DataWatcherObject(ProtocolVersion.SERVER_VERSION.getMarkerPosition(), DataWatcherSerializer.Byte), (byte)16);
		return datawatcher;
    }
}