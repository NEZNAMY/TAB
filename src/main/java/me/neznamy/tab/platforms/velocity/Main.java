package me.neznamy.tab.platforms.velocity;

import java.util.Collection;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.platforms.velocity.protocol.Team;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.kyori.adventure.text.TextComponent;

/**
 * Main class for Velocity platform
 */
@Plugin(id = "tab", name = "TAB", version = "2.8.5", description = "Change a player's tablist prefix/suffix, name tag prefix/suffix, header/footer, bossbar and more", authors = {"NEZNAMY"})
public class Main {

	public ProxyServer server;
	public static PluginMessenger plm;

	@Inject
	public Main(ProxyServer server) {
		this.server = server;
	}
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		if (!hasRequiredLibs()) {
			System.out.println("\u00a7c[TAB] The plugin requires Velocity 1.1.0 build #158 and up to work. Get it at https://ci.velocitypowered.com/job/velocity-1.1.0/");
			return;
		}
		if (!VelocityPacketRegistry.registerPackets()) {
			System.out.println("\u00a7c[TAB] This plugin version does not support your Velocity version. Update the plugin.");
			return;
		}
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.values()[1];
		Shared.platform = new VelocityMethods(server);
		UniversalPacketPlayOut.builder = new VelocityPacketBuilder();
		server.getEventManager().register(this, new VelocityEventListener());
		CommandManager cmd = server.getCommandManager();
		cmd.register(cmd.metaBuilder("btab").build(), new Command() {
			public void execute(CommandSource sender, String[] args) {
				if (Shared.disabled) {
					for (String message : Shared.disabledCommand.execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
						sender.sendMessage(TextComponent.of(Placeholders.color(message)));
					}
				} else {
					Shared.command.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
				}
			}
/*			public List<String> suggest(CommandSource sender, String[] args) {
				List<String> sug = Shared.command.complete(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
				if (sug == null) {
					sug = new ArrayList<String>();
					for (Player p : server.getAllPlayers()) {
						sug.add(p.getUsername());
					}
				}
				return sug;
			}*/
		});
		plm = new PluginMessenger(this);
		Shared.load(true);
	}

	private boolean hasRequiredLibs() {
		try {
			Class.forName("org.yaml.snakeyaml.Yaml"); //1.1.0+
			Class.forName("net.kyori.adventure.text.Component"); //1.1.0 b158+
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static void inject(UUID uuid) {
		Channel channel = Shared.getPlayer(uuid).channel;
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				ITabPlayer player = Shared.getPlayer(uuid);
				if (player == null) {
					super.write(context, packet, channelPromise);
					return;
				}
				Object modifiedPacket = packet;
				try {
					if (modifiedPacket.getClass().getSimpleName().equals("PlayerListItem")) {
						PacketPlayOutPlayerInfo info = VelocityPacketBuilder.readPlayerInfo(modifiedPacket);
						Shared.featureManager.onPacketPlayOutPlayerInfo(player, info);
						modifiedPacket = info.create(player.getVersion());
					}
					if (modifiedPacket instanceof Team && Shared.featureManager.isFeatureEnabled("nametag16")) {
						long time = System.nanoTime();
						modifyPlayers((Team) modifiedPacket);
						Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.PACKET_READING, System.nanoTime()-time);
					}
				} catch (Throwable e){
					Shared.errorManager.printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
				}
				if (modifiedPacket != null) super.write(context, modifiedPacket, channelPromise);
			}
		});
	}
	public static void modifyPlayers(Team packet){
		if (packet.players == null) return;
		if (packet.getFriendlyFire() != 69) {
			Collection<String> col = Lists.newArrayList(packet.getPlayers());
			for (ITabPlayer p : Shared.getPlayers()) {
				if (col.contains(p.getName()) && !p.disabledNametag) {
					col.remove(p.getName());
				}
			}
			packet.players = col.toArray(new String[0]);
		}
	}
}