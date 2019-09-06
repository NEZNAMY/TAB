package me.neznamy.tab.bukkit.unlimitedtags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.bukkit.packets.DataWatcher;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntityMetadata;
import me.neznamy.tab.bukkit.packets.PacketPlayOutSpawnEntityLiving;
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

	private int entityId = Shared.getNextEntityId();
	private UUID uuid = UUID.randomUUID();
	private Location location;
	private boolean sneaking;
	private boolean invisible;

	private List<ITabPlayer> registeredTo = Collections.synchronizedList(new ArrayList<ITabPlayer>());
	private FakeDataWatcher datawatcher;
	private Property property;

	private long lastLocationRefresh = 0;

	public ArmorStand(ITabPlayer owner, String format, double yOffset, String ID) {
		this.owner = owner;
		player = (Player) owner.getPlayer();
		this.ID = ID;
		this.yOffset = yOffset;
		datawatcher = new FakeDataWatcher();
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
		if (property.isUpdateNeeded()) {
			updateMetadata();
		}
	}
	public PacketPlayOutSpawnEntityLiving getSpawnPacket(ITabPlayer to, boolean addToRegistered) {
		updateLocation();
		String name = owner.properties.get(ID).get();
		if (Placeholders.placeholderAPI) name = PlaceholderAPI.setRelationalPlaceholders(player, (Player) to.getPlayer(), name);
		datawatcher.setCustomNameVisible(!(invisible || name.length() == 0 || TABAPI.hasHiddenNametag(player.getUniqueId())));
		DataWatcher w = datawatcher.create(name);
		if (!registeredTo.contains(to) && addToRegistered) registeredTo.add(to);
		return new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, location).setDataWatcher(w);
	}
	public PacketPlayOutEntityTeleport getTeleportPacket() {
		updateLocation();
		return new PacketPlayOutEntityTeleport(entityId, location);
	}
	public PacketPlayOutEntityDestroy getDestroyPacket(ITabPlayer to, boolean removeFromRegistered) {
		if (removeFromRegistered) removeFromRegistered(to);
		return new PacketPlayOutEntityDestroy(entityId);
	}
	public void teleport() {
		PacketPlayOutEntityTeleport packet = getTeleportPacket();
		synchronized (registeredTo) {
			for (ITabPlayer all : registeredTo) packet.send(all);
		}
	}
	public void sneak(boolean sneaking) {
		datawatcher.setSneaking(sneaking);
		this.sneaking = sneaking;
		updateLocation();
		synchronized (registeredTo) {
			for (ITabPlayer all : registeredTo) {
				if (all == owner) continue;
				getDestroyPacket(all, false).send(all);
				if (!(sneaking && all.getVersion().getNumber() >= ProtocolVersion.v1_14.getNumber())) getSpawnPacket(all, false).send(all);
			}
		}
	}
	public void destroy() {
		synchronized (registeredTo) {
			for (ITabPlayer all : registeredTo) getDestroyPacket(all, false).send(all);
		}
		registeredTo.clear();
	}
	public void updateVisibility() {
		if ((!owner.hasInvisibility() && player.getGameMode() != GameMode.SPECTATOR) == invisible) {
			invisible = !invisible;
			updateMetadata();
		}
	}
	private void updateMetadata() {
		synchronized (registeredTo) {
			Property line = owner.properties.get(ID);
			String name = line.get();
			if (Placeholders.placeholderAPI && line.hasRelationalPlaceholders()) {
				for (ITabPlayer all : registeredTo) {
					name = PlaceholderAPI.setRelationalPlaceholders(player, (Player) all.getPlayer(), name);
					datawatcher.setCustomNameVisible(!(invisible || name.length() == 0 || TABAPI.hasHiddenNametag(player.getUniqueId())));
					DataWatcher w = datawatcher.create(name);
					new PacketPlayOutEntityMetadata(entityId, w, true).send(all);
				}
			} else {
				datawatcher.setCustomNameVisible(!(invisible || name.length() == 0 || TABAPI.hasHiddenNametag(player.getUniqueId())));
				DataWatcher w = datawatcher.create(name);
				PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(entityId, w, true);
				for (ITabPlayer all : registeredTo) {
					packet.send(all);
				}
			}
		}
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
}