package me.neznamy.tab.bukkit.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.bukkit.packets.NMSClass;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntityDestroy;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntityMetadata;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntityTeleport;
import me.neznamy.tab.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Placeholders;
import me.neznamy.tab.shared.Shared;

public class ArmorStand{

	private ITabPlayer tabp;
	private Player player;
	private double yOffset;
	private String rawFormat;
	private String ID;

	private int entityId = Shared.getNextEntityId();
	private UUID uuid = UUID.randomUUID();
	private Location location;
	private boolean sneaking;
	private boolean invisible;

	private boolean inBed;
	private List<ITabPlayer> registeredTo = Collections.synchronizedList(new ArrayList<ITabPlayer>());
	private FakeDataWatcher datawatcher;

	private long lastLocationRefresh = 0;

	public ArmorStand(ITabPlayer tabp, String format, double yOffset, String ID) {
		this.tabp = tabp;
		player = (Player) tabp.getPlayer();
		this.ID = ID;
		this.yOffset = yOffset;
		rawFormat = format;
		datawatcher = new FakeDataWatcher(player);
		replaceFormat();
		updateLocation();
	}
	public String getID() {
		return ID;
	}
	public void setNameFormat(String format) {
		rawFormat = format;
	}
	public void replaceFormat() {
		setCustomName(Placeholders.replace(rawFormat, tabp));
	}
	public void setInBed(boolean inBed) {
		this.inBed = inBed;
	}
	public boolean isInBed() {
		return inBed;
	}
	public PacketPlayOutSpawnEntityLiving getSpawnPacket(ITabPlayer to) {
		updateLocation();
		addToRegistered(to);
		return new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, location).setDataWatcher(datawatcher.get(to));
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
		synchronized(registeredTo) {for (ITabPlayer all : registeredTo) packet.send(all);}
	}
	public void sneak(boolean b) {
		if (sneaking == b) return;
		datawatcher.setSneaking(b);
		sneaking = b;
		teleport();
		updateMetadata();
	}
	public void destroy() {
		synchronized(registeredTo) {for (ITabPlayer all : registeredTo) getDestroyPacket(all, false).send(all);}
		registeredTo.clear();
	}
	public void updateVisibility() {
		if ((!player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.getGameMode() != GameMode.SPECTATOR) == invisible) {
			invisible = !invisible;
			updateMetadata();
		}
	}
	public void updateMetadata() {
		synchronized(registeredTo) {
			for (ITabPlayer all : registeredTo) {
				boolean alreadyInvisible = !datawatcher.isCustomNameVisible();
				if (invisible || alreadyInvisible || TABAPI.hasHiddenNametag(player.getUniqueId())) datawatcher.setCustomNameVisible(false);
				new PacketPlayOutEntityMetadata(entityId, datawatcher.get(all), true).send(all);
			}
		}
	}
	private void setCustomName(String format) {
		datawatcher.setCustomName(format);
		datawatcher.setCustomNameVisible(format.length() > 0 && !invisible);
	}
	private void updateLocation() {
		if (System.currentTimeMillis() - lastLocationRefresh < 50) return;
		lastLocationRefresh = System.currentTimeMillis();
		double x = player.getLocation().getX();
		double y;
		double z = player.getLocation().getZ();
		if (inBed) {
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
	public void addToRegistered(ITabPlayer added) {
		if (!registeredTo.contains(added)) registeredTo.add(added);
	}
}