package me.neznamy.tab.platforms.bukkit.unlimitedtags;

import java.util.ArrayList;
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
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

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

	private List<ITabPlayer> registeredTo = new ArrayList<ITabPlayer>();
	private Property property;

	private long lastLocationRefresh = 0;
	
	public ArmorStand(ITabPlayer owner, String format, double yOffset, String ID) {
		this.owner = owner;
		player = ((TabPlayer)owner).player;
		this.yOffset = yOffset;
		owner.setProperty(ID, format);
		property = owner.properties.get(ID);
		visible = getVisibility();
		refreshName();
		updateLocation();
	}
	public void refreshName() {
		if (property.isUpdateNeeded()) {
			visible = getVisibility();
			updateMetadata();
		}
	}
	public PacketPlayOutSpawnEntityLiving getSpawnPacket(ITabPlayer to, boolean addToRegistered) {
		updateLocation();
		visible = getVisibility();
		String name = property.get();
		name = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, to, name);
		if (!registeredTo.contains(to) && addToRegistered) registeredTo.add(to);
		return new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.valueOf("ARMOR_STAND"), location).setDataWatcher(createDataWatcher(name, to));
	}
	public Object getNMSTeleportPacket() {
		updateLocation();
		return MethodAPI.getInstance().newPacketPlayOutEntityTeleport(nmsEntity, location);
	}
	public void destroy(ITabPlayer to) {
		registeredTo.remove(to);
		to.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
	}
	public void teleport() {
		Object teleportPacket = getNMSTeleportPacket();
		for (ITabPlayer all : registeredTo.toArray(new ITabPlayer[0])) all.sendPacket(teleportPacket);
	}
	public void sneak(boolean sneaking) {
		this.sneaking = sneaking;
		updateLocation();
		for (ITabPlayer all : registeredTo.toArray(new ITabPlayer[0])) {
			if (all == owner) continue; //should never be anyway
			if (all.getVersion().getNetworkId() >= ProtocolVersion.v1_14.getNetworkId()) {
				//sneaking feature was removed in 1.14, so despawning completely now
				if (sneaking) {
					all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
				} else {
					all.sendCustomPacket(getSpawnPacket(all, false));
				}
			} else {
				//respawning so there's no animation and it's instant
				all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
				all.sendCustomPacket(getSpawnPacket(all, false));
			}
		}
	}
	public void destroy() {
		Object destroyPacket = MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId);
		for (ITabPlayer all : registeredTo.toArray(new ITabPlayer[0])) all.sendPacket(destroyPacket);
		registeredTo.clear();
	}
	public void updateVisibility() {
		if (getVisibility() != visible) {
			visible = !visible;
			updateMetadata();
		}
	}
	private void updateMetadata() {
		String name = property.get();
		if (property.hasRelationalPlaceholders()) {
			for (ITabPlayer all : registeredTo.toArray(new ITabPlayer[0])) {
				String currentName = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, all, name);
				all.sendPacket(new PacketPlayOutEntityMetadata(entityId, createDataWatcher(currentName, all), true).toNMS(null));
			}
			if (owner.previewingNametag) {
				String currentName = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, owner, name);
				owner.sendPacket(new PacketPlayOutEntityMetadata(entityId, createDataWatcher(currentName, owner), true).toNMS(null));
			}
		} else {
			for (ITabPlayer all : registeredTo.toArray(new ITabPlayer[0])) {
				all.sendPacket(new PacketPlayOutEntityMetadata(entityId, createDataWatcher(name, all), true).toNMS(null));
			}
			if (owner.previewingNametag) owner.sendPacket(new PacketPlayOutEntityMetadata(entityId, createDataWatcher(name, owner), true).toNMS(null));
		}
	}
	public boolean getVisibility() {
		return !owner.hasInvisibility() && player.getGameMode() != GameMode.valueOf("SPECTATOR") && !TABAPI.hasHiddenNametag(player.getUniqueId()) && property.get().length() > 0;
	}
	private void updateLocation() {
		if (System.currentTimeMillis() - lastLocationRefresh < 50) return;
		lastLocationRefresh = System.currentTimeMillis();
		double x = player.getLocation().getX();
		double y = player.getLocation().getY() + yOffset;
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
		y += 2;
		location = new Location(null,x,y,z);
	}
	public int getEntityId() {
		return entityId;
	}
	public void removeFromRegistered(ITabPlayer removed) {
		registeredTo.remove(removed);
	}
	public DataWatcher createDataWatcher(String name, ITabPlayer other) {
		byte flag = 0;
		if (sneaking) flag += (byte)2;
		flag += (byte)32;
		DataWatcher datawatcher = new DataWatcher(null);
		if (name == null) name = "";
		datawatcher.setValue(new DataWatcherObject(0, DataWatcherSerializer.Byte), flag);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.Optional_IChatBaseComponent), Optional.ofNullable(MethodAPI.getInstance().ICBC_fromString(Shared.jsonFromText(name))));
		} else {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.String), name);
		}
		boolean visible = (name.length() == 0 || !((TabPlayer)other).player.canSee(player)) ? false : this.visible;
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Boolean), visible);
		} else {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Byte), (byte)(visible?1:0));
		}
		datawatcher.setValue(new DataWatcherObject(ProtocolVersion.SERVER_VERSION.getMarkerPosition(), DataWatcherSerializer.Byte), (byte)16);
		return datawatcher;
	}
}