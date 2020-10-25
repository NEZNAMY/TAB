package me.neznamy.tab.platforms.proxy.velocity;

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
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.proxy.PluginMessageHandler;
import me.neznamy.tab.platforms.proxy.velocity.protocol.Team;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

/**
 * Main class for Velocity platform
 */
@Plugin(id = "tab", name = "TAB", version = Shared.pluginVersion, description = "Change player tablist prefix/suffix, name tag prefix/suffix, header/footer, bossbar and more", authors = {"NEZNAMY"})
public class Main {

	//instance of proxyserver
	public ProxyServer server;
	
	//plugin message handler
	public static PluginMessageHandler plm;

	@Inject
	public Main(ProxyServer server) {
		this.server = server;
	}
	
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		if (!hasRequiredLibs()) {
			System.out.println("\u00a7c[TAB] The plugin requires Velocity 1.1.0 build #265 and up to work. Get it at https://ci.velocitypowered.com/job/velocity-1.1.0/");
			return;
		}
		if (!VelocityPacketRegistry.registerPackets()) {
			System.out.println("\u00a7c[TAB] Your velocity version is way too new for this plugin version. Update the plugin or downgrade Velocity.");
			return;
		}
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.values()[1];
		Shared.platform = new VelocityMethods(server);
		server.getEventManager().register(this, new VelocityEventListener());
		CommandManager cmd = server.getCommandManager();
		cmd.register(cmd.metaBuilder("btab").build(), new Command() {
			public void execute(CommandSource sender, String[] args) {
				if (Shared.disabled) {
					for (String message : Shared.disabledCommand.execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
						sender.sendMessage(Identity.nil(), Component.text(Placeholders.color(message)));
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
		plm = new VelocityPluginMessageHandler(this);
		Shared.load(true);
	}

	/**
	 * Check for required libraries and returns true if server has all required libs, false if not
	 * @return true if version is supported, false if not
	 */
	private boolean hasRequiredLibs() {
		try {
			Class.forName("org.yaml.snakeyaml.Yaml"); //1.1.0+
			Class.forName("net.kyori.adventure.identity.Identity"); //1.1.0 b265
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Injects custom channel duplex handler to prevent other plugins from overriding this one
	 * @param uuid - player's uuid
	 */
	public static void inject(UUID uuid) {
		Channel channel = Shared.getPlayer(uuid).getChannel();
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				TabPlayer player = Shared.getPlayer(uuid);
				if (player == null) {
					super.write(context, packet, channelPromise);
					return;
				}
				try {
					if (packet.getClass().getSimpleName().equals("PlayerListItem") && player.getVersion().getMinorVersion() >= 8) {
						PacketPlayOutPlayerInfo info = UniversalPacketPlayOut.builder.readPlayerInfo(packet, player.getVersion());
						Shared.featureManager.onPacketPlayOutPlayerInfo(player, info);
						super.write(context, info.create(player.getVersion()), channelPromise);
						return;
					}
					if (packet instanceof Team && Shared.featureManager.isFeatureEnabled("nametag16")) {
						long time = System.nanoTime();
						modifyPlayers((Team) packet);
						Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
						super.write(context, packet, channelPromise);
						return;
					}
				} catch (Throwable e){
					Shared.errorManager.printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	
	/**
	 * Removes all real players from packet if the packet doesn't come from TAB
	 * @param packet - packet to modify
	 */
	private static void modifyPlayers(Team packet){
		if (packet.players == null) return;
		if (packet.getFriendlyFire() != 69) {
			Collection<String> col = Lists.newArrayList(packet.getPlayers());
			for (TabPlayer p : Shared.getPlayers()) {
				if (col.contains(p.getName()) && !Shared.featureManager.getNameTagFeature().isDisabledWorld(p.getWorldName())) {
					col.remove(p.getName());
				}
			}
			packet.players = col.toArray(new String[0]);
		}
	}
}