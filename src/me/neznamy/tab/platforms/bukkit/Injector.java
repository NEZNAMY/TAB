package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.ArmorStand;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagX;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagXPacket;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.Playerlist;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

public class Injector {

	public static void inject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (!channel.pipeline().names().contains("packet_handler")) {
			//waterfall bug
//			Shared.error(null, "Failed to inject " + Shared.getPlayer(uuid).getName() + ", packet_handler does not exist");
			return;
		}
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("packet_handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				if (Shared.disabled) {
					super.channelRead(context, packet);
					return;
				}
				try{
					ITabPlayer player = Shared.getPlayer(uuid);
					if (player == null) {
						//wtf
						super.channelRead(context, packet);
						return;
					}
					if (Configs.unlimitedTags && player.getVersion().getMinorVersion() == 8) {
						if (MethodAPI.PacketPlayInUseEntity.isInstance(packet)) {
							Field a = MethodAPI.PacketPlayInUseEntity.getDeclaredField("a");
							a.setAccessible(true);
							int entityId = a.getInt(packet);
							ITabPlayer attacked = null;
							loop:
							for (ITabPlayer all : Shared.getPlayers()) {
								for (ArmorStand as : all.getArmorStands()) {
									if (as.getEntityId() == entityId) {
										attacked = all;
										break loop;
									}
								}
							}
							if (attacked != null && attacked != player) {
								a.set(packet, ((TabPlayer)attacked).player.getEntityId());
							}
						}
					}
				} catch (Throwable e){
					Shared.error(null, "An error occurred when reading packets", e);
				}
				super.channelRead(context, packet);
			}
			@SuppressWarnings("unchecked")
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				if (Shared.disabled) {
					super.write(context, packet, channelPromise);
					return;
				}
				try{
					ITabPlayer player = Shared.getPlayer(uuid);
					if (player == null) {
						//wtf
						super.write(context, packet, channelPromise);
						return;
					}
					long time = System.nanoTime();
					if (MethodAPI.PacketPlayOutScoreboardTeam.isInstance(packet)) {
						//nametag anti-override
						if ((NameTag16.enable || NameTagX.enable) && Main.killPacket(packet)) {
							Shared.cpu.addFeatureTime("Nametag anti-override", System.nanoTime()-time);
							return;
						}
					}
					Shared.cpu.addFeatureTime("Nametag anti-override", System.nanoTime()-time);

					if (NameTagX.enable) {
						time = System.nanoTime();
						NameTagXPacket pack = NameTagXPacket.fromNMS(packet);
						if (pack != null) {
							ITabPlayer packetPlayer = null;
							if (pack.a != null && pack.a instanceof Integer) {
								packetPlayer = Shared.getPlayer((int)pack.a);
							}
							if (packetPlayer == null && pack.b != null && pack.b instanceof Integer) {
								packetPlayer = Shared.getPlayer((int)pack.b);
							}
							if (packetPlayer == null || !packetPlayer.disabledNametag) {
								//sending packets outside of the packet reader or protocollib will cause problems
								Shared.runTask("processing packet out", "NameTagX - processing", new Runnable() {
									public void run() {
										NameTagX.processPacketOUT(pack, player);
									}
								});
							}
						}
						Shared.cpu.addFeatureTime("NameTagX - reading", System.nanoTime()-time);
					}
					PacketPlayOut p = null;

					time = System.nanoTime();
					if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9 && Configs.fixPetNames) {
						//preventing pets from having owner's nametag properties if feature is enabled
						if (MethodAPI.PacketPlayOutEntityMetadata.isInstance(packet)) {
							List<Object> items = (List<Object>) MethodAPI.PacketPlayOutEntityMetadata_LIST.get(packet);
							List<Object> newList = new ArrayList<Object>();
							for (Object item : items) {
								Item i = Item.fromNMS(item);
								if (i.type.position == ProtocolVersion.SERVER_VERSION.getPetOwnerPosition()) {
									modifyDataWatcherItem(i);
								}
								newList.add(i.toNMS());
							}
							MethodAPI.PacketPlayOutEntityMetadata_LIST.set(packet, newList);
						}
						if (MethodAPI.PacketPlayOutSpawnEntityLiving.isInstance(packet) && PacketPlayOutSpawnEntityLiving.DATAWATCHER != null) {
							DataWatcher watcher = DataWatcher.fromNMS(PacketPlayOutSpawnEntityLiving.DATAWATCHER.get(packet));
							Item petOwner = watcher.getItem(ProtocolVersion.SERVER_VERSION.getPetOwnerPosition());
							if (petOwner != null) modifyDataWatcherItem(petOwner);
							PacketPlayOutSpawnEntityLiving.DATAWATCHER.set(packet, watcher.toNMS());
						}
					}
					Shared.cpu.addFeatureTime("Other", System.nanoTime()-time);
					if (Playerlist.enable) {
						//correcting name, spectators if enabled, changing npc names if enabled
						time = System.nanoTime();
						if ((p = PacketPlayOutPlayerInfo.fromNMS(packet)) != null) {
							Playerlist.modifyPacket((PacketPlayOutPlayerInfo) p, player);
							packet = p.toNMS(null);
						}
						Shared.cpu.addFeatureTime("Tablist names 2", System.nanoTime()-time);
					}
				} catch (Throwable e){
					Shared.error(null, "An error occurred when reading packets", e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	public static void uninject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
	}
	@SuppressWarnings({ "rawtypes" })
	private static void modifyDataWatcherItem(Item petOwner) {
		//1.12-
		if (petOwner.value instanceof com.google.common.base.Optional) {
			com.google.common.base.Optional o = (com.google.common.base.Optional) petOwner.value;
			if (o.isPresent() && o.get() instanceof UUID) {
				petOwner.value = com.google.common.base.Optional.of(UUID.randomUUID());
			}
		}
		//1.13+
		if (petOwner.value instanceof java.util.Optional) {
			java.util.Optional o = (java.util.Optional) petOwner.value;
			if (o.isPresent() && o.get() instanceof UUID) {
				petOwner.value = java.util.Optional.of(UUID.randomUUID());
			}
		}
	}
}
