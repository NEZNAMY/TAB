package me.neznamy.tab.shared;

import java.util.*;

import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import org.geysermc.floodgate.api.FloodgateApi;

import io.netty.channel.Channel;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.api.protocol.PacketPlayOutChat.ChatMessageType;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.api.team.TeamManager;

/**
 * The core class for player
 */
public abstract class ITabPlayer implements TabPlayer {

	protected final Object player;
	private final String name;
	private final UUID uniqueId;
	private String world;
	private String server;
	private String permissionGroup = GroupManager.DEFAULT_GROUP;
	private String teamName;
	private String teamNameNote;
	private final boolean bedrockPlayer;

	private final Map<String, Property> properties = new HashMap<>();
	private ArmorStandManager armorStandManager;
	protected ProtocolVersion version;
	protected Channel channel;

	private boolean previewingNameTag;
	private boolean onJoinFinished;

	private List<String> registeredTeams = new ArrayList<>();

	protected ITabPlayer(Object player, UUID uniqueId, String name, String server, String world) {
		this.player = player;
		this.uniqueId = uniqueId;
		this.name = name;
		this.server = server;
		this.world = world;
		bedrockPlayer = TAB.getInstance().isFloodgateInstalled() && FloodgateApi.getInstance().isFloodgatePlayer(uniqueId);
		setGroup(TAB.getInstance().getGroupManager().detectPermissionGroup(this), false);
	}

	@Override
	public void sendMessage(String message, boolean translateColors) {
		if (message == null || message.length() == 0) return;
		IChatBaseComponent component;
		if (translateColors) {
			component = IChatBaseComponent.fromColoredText(message);
		} else {
			component = new IChatBaseComponent(message);
		}
		sendCustomPacket(new PacketPlayOutChat(component, ChatMessageType.CHAT));
	}

	@Override
	public void sendMessage(IChatBaseComponent message) {
		sendCustomPacket(new PacketPlayOutChat(message, ChatMessageType.CHAT));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UUID getUniqueId() {
		return uniqueId;
	}

	@Override
	public UUID getTablistUUID() {
		return uniqueId;
	}

	@Override
	public void forceRefresh() {
		TAB.getInstance().getFeatureManager().refresh(this, true);
	}

	@Override
	public ProtocolVersion getVersion() {
		return version;
	}

	@Override
	public String getWorld() {
		return world;
	}
	
	@Override
	public String getServer() {
		return server;
	}

	@Override
	public void sendCustomPacket(TabPacket packet) {
		if (packet == null) return;
		if (packet instanceof PacketPlayOutScoreboardTeam) {
			String team = ((PacketPlayOutScoreboardTeam) packet).getName();
			if (((PacketPlayOutScoreboardTeam) packet).getMethod() == 0) {
				if (registeredTeams.contains(team)) {
					TAB.getInstance().getErrorManager().printError("Tried to register duplicated team " + team + " to player " + getName());
					return;
				}
				registeredTeams.add(team);
			} else if (((PacketPlayOutScoreboardTeam) packet).getMethod() == 1) {
				registeredTeams.remove(((PacketPlayOutScoreboardTeam) packet).getName());
			}
		}
		try {
			sendPacket(TAB.getInstance().getPlatform().getPacketBuilder().build(packet, getVersion()));
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("An error occurred when creating " + packet.getClass().getSimpleName(), e);
		}
	}

	@Override
	public void sendCustomPacket(TabPacket packet, TabFeature feature) {
		sendCustomPacket(packet);
		if (feature != null) TAB.getInstance().getCPUManager().packetSent(feature.getFeatureName());
	}
	
	@Override
	public void sendCustomPacket(TabPacket packet, String feature) {
		sendCustomPacket(packet);
		if (feature != null) TAB.getInstance().getCPUManager().packetSent(feature);
	}
	
	@Override
	public void sendPacket(Object nmsPacket, TabFeature feature) {
		sendPacket(nmsPacket);
		if (feature != null) TAB.getInstance().getCPUManager().packetSent(feature.getFeatureName());
	}

	@Override
	public void sendPacket(Object packet, String feature) {
		sendPacket(packet);
		if (feature != null) TAB.getInstance().getCPUManager().packetSent(feature);
	}

	@Override
	public Property getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public String getGroup() {
		return permissionGroup;
	}

	@Override
	public void toggleNametagPreview() {
		if (armorStandManager == null) throw new IllegalStateException("Unlimited NameTag mode is not enabled");
		if (previewingNameTag) {
			armorStandManager.destroy(this);
			sendMessage(TAB.getInstance().getConfiguration().getMessages().getNametagPreviewOff(), true);
		} else {
			armorStandManager.spawn(this);
			sendMessage(TAB.getInstance().getConfiguration().getMessages().getNametagPreviewOn(), true);
		}
		previewingNameTag = !previewingNameTag;
	}

	@Override
	public boolean isPreviewingNametag() {
		return previewingNameTag;
	}

	@Override
	public Channel getChannel() {
		return channel;
	}

	@Override
	public boolean isLoaded() {
		return onJoinFinished;
	}

	@Override
	public void loadPropertyFromConfig(TabFeature feature, String property) {
		loadPropertyFromConfig(feature, property, "");
	}

	@Override
	public void loadPropertyFromConfig(TabFeature feature, String property, String ifNotSet) {
		String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(getName(), property, server, world);
		if (value.length == 0) {
			value = TAB.getInstance().getConfiguration().getUsers().getProperty(getUniqueId().toString(), property, server, world);
		}
		if (value.length == 0) {
			value = TAB.getInstance().getConfiguration().getGroups().getProperty(getGroup(), property, server, world);
		}
		if (value.length > 0) {
			setProperty(feature, property, value[0], value[1]);
			return;
		}
		setProperty(feature, property, ifNotSet, "None");
	}

	@Override
	public ArmorStandManager getArmorStandManager() {
		return armorStandManager;
	}

	@Override
	public void setArmorStandManager(ArmorStandManager armorStandManager) {
		this.armorStandManager = armorStandManager;
	}

	@Override
	public String getTeamName() {
		TeamManager teams = TAB.getInstance().getTeamManager();
		if (teams != null && teams.getForcedTeamName(this) != null) return teams.getForcedTeamName(this);
		return teamName;
	}

	@Override
	public String getTeamNameNote() {
		return teamNameNote;
	}
	
	@Override
	public boolean isBedrockPlayer() {
		return bedrockPlayer;
	}
	
	@Override
	public boolean setProperty(TabFeature feature, String identifier, String rawValue) {
		return setProperty(feature, identifier, rawValue, null);
	}
	
	private boolean setProperty(TabFeature feature, String identifier, String rawValue, String source) {
		PropertyImpl p = (PropertyImpl) getProperty(identifier);
		if (p == null) {
			properties.put(identifier, new PropertyImpl(feature, this, rawValue, source));
			return true;
		} else {
			if (!p.getOriginalRawValue().equals(rawValue)) {
				p.changeRawValue(rawValue);
				p.setSource(source);
				return true;
			}
			return false;
		}
	}
	
	public void setTeamNameNote(String note) {
		teamNameNote = note;
	}
	
	public void setTeamName(String name) {
		teamName = name;
	}
	
	public void markAsLoaded() {
		onJoinFinished = true;
		TAB.getInstance().getPlatform().callLoadEvent(this);
	}

	public void setGroup(String permissionGroup, boolean refreshIfChanged) {
		if (this.permissionGroup.equals(permissionGroup)) return;
		if (permissionGroup != null) {
			this.permissionGroup = permissionGroup;
		} else {
			this.permissionGroup = GroupManager.DEFAULT_GROUP;
		}
		if (refreshIfChanged) {
			forceRefresh();
		}
	}

	public void setWorld(String name) {
		world = name;
	}
	
	public void setServer(String name) {
		server = name;
	}
}
