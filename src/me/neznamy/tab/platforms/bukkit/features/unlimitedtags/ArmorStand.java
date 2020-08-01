package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

public class ArmorStand{

	private ITabPlayer owner;
	private Player player;
	private double yOffset;
	private Object nmsEntity = MethodAPI.getInstance().newEntityArmorStand();
	private int entityId = MethodAPI.getInstance().getEntityId(nmsEntity);
	private UUID uuid = UUID.randomUUID();
	private boolean sneaking;
	private boolean visible;

	private List<ITabPlayer> nearbyPlayers = Collections.synchronizedList(new ArrayList<ITabPlayer>());
	public Property property;
	private boolean staticOffset;
	private boolean markerFor18x;

	public ArmorStand(ITabPlayer owner, Property property, double yOffset, boolean staticOffset) {
		this.owner = owner;
		this.staticOffset = staticOffset;
		player = owner.getBukkitEntity();
		this.yOffset = yOffset;
		this.property = property;
		markerFor18x = ((NameTagX)Shared.features.get("nametagx")).markerFor18x;
		refresh();
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
		for (ITabPlayer all : getNearbyPlayers()) {
			all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityTeleport(nmsEntity, getArmorStandLocationFor(all)));
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
		return viewer.getVersion().getMinorVersion() == 8 && !markerFor18x ? getLocation().clone().add(0,-2,0) : getLocation();
	}
	public void destroy(ITabPlayer viewer) {
		nearbyPlayers.remove(viewer);
		viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(entityId));
	}
	public void teleport() {
			for (ITabPlayer all : getNearbyPlayers()) {
				all.sendPacket(getTeleportPacket(all));
			}
	}
	public void sneak(boolean sneaking) {
		this.sneaking = sneaking;
			for (ITabPlayer viewer : getNearbyPlayers()) {
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
			for (ITabPlayer viewer : getNearbyPlayers()) {
				viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(entityId, createDataWatcher(property.getFormat(viewer), viewer).toNMS(), true));
			}
	}
	public boolean getVisibility() {
		if (Configs.SECRET_armorstands_always_visible) return true;
		return !owner.hasInvisibility() && player.getGameMode() != GameMode.SPECTATOR && !owner.hasHiddenNametag() && property.get().length() > 0;
	}
	public Location getLocation() {
		double x = player.getLocation().getX();
		double y = player.getLocation().getY() + yOffset + 2;
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
		return new Location(null,x,y,z);
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
		datawatcher.helper().setEntityFlags(flag);

		if (displayName == null) displayName = "";
		datawatcher.helper().setCustomName(displayName, viewer.getVersion());

		boolean visible = (isNameVisiblyEmpty(displayName) || !viewer.getBukkitEntity().canSee(player)) ? false : this.visible;
		datawatcher.helper().setCustomNameVisible(visible);

		if (viewer.getVersion().getMinorVersion() > 8 || markerFor18x) datawatcher.helper().setArmorStandFlags((byte)16);
		return datawatcher;
	}
	public List<ITabPlayer> getNearbyPlayers(){
		return new ArrayList<ITabPlayer>(nearbyPlayers);
	}
	private boolean isNameVisiblyEmpty(String displayName) {
		return IChatBaseComponent.fromColoredText(displayName).toRawText().replace(" ", "").length() == 0;
	}
}