package me.neznamy.tab.bukkit.packets;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Placeholders;
import me.neznamy.tab.shared.Shared;

public class ArmorStand{

	private ITabPlayer owner;
	private Player player;
	private double yOffset;
	private String ID;
	private String rawFormat;
	private String lastReplacedFormat = "";

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
		rawFormat = format;
		datawatcher = new FakeDataWatcher();
		refreshName();
		updateLocation();
	}
	public String getID() {
		return ID;
	}
	public void setNameFormat(String format) {
		rawFormat = format;
	}
	public void refreshName() {
		String newFormat = Placeholders.replace(rawFormat, owner);
		if (newFormat.equals(lastReplacedFormat) && !newFormat.contains("%rel_")) return;
		lastReplacedFormat = newFormat;
		updateMetadata(false);
	}
	public PacketPlayOutSpawnEntityLiving getSpawnPacket(ITabPlayer to, boolean addToRegistered) {
		updateLocation();
		String name = lastReplacedFormat;
		if (me.neznamy.tab.shared.Placeholders.relationalPlaceholders) name = PlaceholderAPI.setRelationalPlaceholders(player, (Player) to.getPlayer(), name);
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
	public void sneak(ITabPlayer packetReceiver, boolean b) {
		if (packetReceiver == owner) return;
		datawatcher.setSneaking(b);
		sneaking = b;
		updateLocation();
		getDestroyPacket(packetReceiver, false).send(packetReceiver);
		getSpawnPacket(packetReceiver, false).send(packetReceiver);
	}
	public void destroy() {
		for (ITabPlayer all : registeredTo.keySet()) getDestroyPacket(all, false).send(all);
		registeredTo.clear();
	}
	public void updateVisibility() {
		if ((!player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.getGameMode() != GameMode.SPECTATOR) == invisible) {
			invisible = !invisible;
			updateMetadata(true);
		}
	}
	private void updateMetadata(boolean force) {
		for (Entry<ITabPlayer, String> entry : registeredTo.entrySet()) {
			ITabPlayer all = entry.getKey();
			String lastName = entry.getValue();
			String name = lastReplacedFormat;
			if (me.neznamy.tab.shared.Placeholders.relationalPlaceholders) name = PlaceholderAPI.setRelationalPlaceholders(player, (Player) all.getPlayer(), name);
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
			if (NMSClass.versionNumber >= 9) {
				y = player.getLocation().getY() + yOffset - (sneaking ? 0.45 : 0.18);
			} else {
				y = player.getLocation().getY() + yOffset - (sneaking ? 0.30 : 0.18);
			}
		}
		location = new Location(null,x,y,z);
	}
	public int getEntityId() {
		return entityId;
	}
	public void removeFromRegistered(ITabPlayer removed) {
		registeredTo.remove(removed);
	}
}