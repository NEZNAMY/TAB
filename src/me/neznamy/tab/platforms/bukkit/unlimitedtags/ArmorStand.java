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

	private List<ITabPlayer> registeredTo = Collections.synchronizedList(new ArrayList<ITabPlayer>());
	public Property property;
	private boolean staticOffset;
	private long lastLocationRefresh = 0;
	
	public ArmorStand(ITabPlayer owner, String format, double yOffset, String ID, boolean staticOffset) {
		this.owner = owner;
		this.staticOffset = staticOffset;
		player = ((TabPlayer)owner).player;
		this.yOffset = yOffset;
		owner.setProperty(ID, format);
		property = owner.properties.get(ID);
		visible = getVisibility();
		refreshName();
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
		updateLocation();
		synchronized (registeredTo) {
			for (ITabPlayer all : registeredTo) {
				all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityTeleport(nmsEntity, getArmorStandLocationFor(all)));
			}
		}
	}
	public PacketPlayOutSpawnEntityLiving getSpawnPacket(ITabPlayer to, boolean addToRegistered) {
		if (to == null) {
			return Shared.error(null, "Attempted to spawn armor stand for null player");
		}
		updateLocation();
		visible = getVisibility();
		String name = property.get();
		if (property.hasRelationalPlaceholders()) name = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, to, name);
		if (!registeredTo.contains(to) && addToRegistered) registeredTo.add(to);
		return new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(to)).setDataWatcher(createDataWatcher(name, to));
	}
	public Object getNMSTeleportPacket(ITabPlayer to) {
		updateLocation();
		return MethodAPI.getInstance().newPacketPlayOutEntityTeleport(nmsEntity, getArmorStandLocationFor(to));
	}
	private Location getArmorStandLocationFor(ITabPlayer to) {
		return to.getVersion().getMinorVersion() == 8 ? location.clone().add(0,-2,0) : location;
	}
	public void destroy(ITabPlayer to) {
		registeredTo.remove(to);
		to.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
	}
	public void teleport() {
		synchronized (registeredTo) {
			for (ITabPlayer all : registeredTo) {
				Object teleportPacket = getNMSTeleportPacket(all);
				all.sendPacket(teleportPacket);
			}
		}
	}
	public void sneak(boolean sneaking) {
		this.sneaking = sneaking;
		updateLocation();
		synchronized (registeredTo) {
			for (ITabPlayer all : registeredTo) {
				if (all == owner) continue; //should never be anyway
				String displayName = property.get();
				if (property.hasRelationalPlaceholders()) displayName = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, all, displayName);
				if (all.getVersion().getMinorVersion() >= 14 && !Configs.SECRET_armorstands_always_visible) {
					//sneaking feature was removed in 1.14, so despawning completely now
					if (sneaking) {
						all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
					} else {
						all.sendCustomPacket(getSpawnPacket(all, false));
						if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
							all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(getEntityId(), createDataWatcher(displayName, all).toNMS(), true));
						}
					}
				} else {
					//respawning so there's no animation and it's instant
					all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
					all.sendCustomPacket(getSpawnPacket(all, false));
					if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
						all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(getEntityId(), createDataWatcher(displayName, all).toNMS(), true));
					}
				}
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
					String currentName = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, all, name);
					all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, createDataWatcher(currentName, all).toNMS(), true));
				}
				if (owner.previewingNametag) {
					String currentName = PluginHooks.PlaceholderAPI_setRelationalPlaceholders(owner, owner, name);
					owner.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, createDataWatcher(currentName, owner).toNMS(), true));
				}
			} else {
				for (ITabPlayer all : registeredTo) {
					all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, createDataWatcher(name, all).toNMS(), true));
				}
				if (owner.previewingNametag) owner.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, createDataWatcher(name, owner).toNMS(), true));
			}
		}
	}
	public boolean getVisibility() {
		if (Configs.SECRET_armorstands_always_visible) return true;
		return !owner.hasInvisibility() && player.getGameMode() != GameMode.SPECTATOR && !TABAPI.hasHiddenNametag(owner.getUniqueId()) && property.get().length() > 0;
	}
	private void updateLocation() {
		if (System.currentTimeMillis() - lastLocationRefresh < 50) return;
		lastLocationRefresh = System.currentTimeMillis();
		double x = player.getLocation().getX();
		double y = player.getLocation().getY() + yOffset;
		double z = player.getLocation().getZ();
		if (player.isSleeping()) {
			y += 1.76;
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
}