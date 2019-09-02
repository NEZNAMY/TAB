package me.neznamy.tab.bukkit.unlimitedtags;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

	private ConcurrentHashMap<ITabPlayer, String> registeredTo = new ConcurrentHashMap<ITabPlayer, String>();
	private FakeDataWatcher datawatcher;

	private long lastLocationRefresh = 0;

	public ArmorStand(ITabPlayer owner, String format, double yOffset, String ID) {
		this.owner = owner;
		player = (Player) owner.getPlayer();
		this.ID = ID;
		this.yOffset = yOffset;
		datawatcher = new FakeDataWatcher();
		owner.setProperty(ID, format);
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
		if (owner.getProperty(ID).isUpdateNeeded()) {
			updateMetadata(false);
		}
	}
	public PacketPlayOutSpawnEntityLiving getSpawnPacket(ITabPlayer to, boolean addToRegistered) {
		updateLocation();
		String name = owner.getProperty(ID).get();
		if (Placeholders.placeholderAPI) name = PlaceholderAPI.setRelationalPlaceholders(player, (Player) to.getPlayer(), name);
		datawatcher.setCustomNameVisible(!(invisible || name.length() == 0 || TABAPI.hasHiddenNametag(player.getUniqueId())));
		DataWatcher w = datawatcher.create(name);
		if (!registeredTo.contains(to) && addToRegistered) registeredTo.put(to, name);
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
		for (ITabPlayer all : registeredTo.keySet()) packet.send(all);
	}
	public void sneak(boolean sneaking) {
		datawatcher.setSneaking(sneaking);
		this.sneaking = sneaking;
		updateLocation();
		for (ITabPlayer all : registeredTo.keySet()) {
			if (all == owner) continue;
			getDestroyPacket(all, false).send(all);
			if (!(sneaking && all.getVersion().getNumber() >= ProtocolVersion.v1_14.getNumber())) getSpawnPacket(all, false).send(all);
		}
	}
	public void destroy() {
		for (ITabPlayer all : registeredTo.keySet()) getDestroyPacket(all, false).send(all);
		registeredTo.clear();
	}
	public void updateVisibility() {
		if ((!owner.hasInvisibility() && player.getGameMode() != GameMode.SPECTATOR) == invisible) {
			invisible = !invisible;
			updateMetadata(true);
		}
	}
	private void updateMetadata(boolean force) {
		for (Entry<ITabPlayer, String> entry : registeredTo.entrySet()) {
			ITabPlayer all = entry.getKey();
			String lastName = entry.getValue();
			String name = owner.getProperty(ID).get();
			if (Placeholders.placeholderAPI) name = PlaceholderAPI.setRelationalPlaceholders(player, (Player) all.getPlayer(), name);
			datawatcher.setCustomNameVisible(!(invisible || name.length() == 0 || TABAPI.hasHiddenNametag(player.getUniqueId())));
			DataWatcher w = datawatcher.create(name);
			if (lastName.equals(name) && !force) continue;
			registeredTo.put(all, name);
			new PacketPlayOutEntityMetadata(entityId, w, true).send(all);
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