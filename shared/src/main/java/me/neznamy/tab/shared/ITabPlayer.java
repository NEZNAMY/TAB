package me.neznamy.tab.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.netty.channel.Channel;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.EnumProperty;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutChat.ChatMessageType;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

/**
 * The core class for player
 */
public abstract class ITabPlayer implements TabPlayer {

	protected String name;
	protected UUID uniqueId;
	protected String world;
	protected String server;
	private String permissionGroup = GroupRefresher.DEFAULT_GROUP;
	private String teamName;
	private String teamNameNote;
	private String forcedTeamName;

	private Map<String, Property> properties = new HashMap<>();
	private ArmorStandManager armorStandManager;
	protected ProtocolVersion version;
	protected Channel channel;

	private boolean previewingNametag;
	private boolean onJoinFinished;

	protected void init() {
		setGroup(((GroupRefresher)TAB.getInstance().getFeatureManager().getFeature("group")).detectPermissionGroup(this), false);
	}

	private void setProperty(String identifier, String rawValue, String source) {
		Property p = getProperty(identifier);
		if (p == null) {
			properties.put(identifier, new Property(this, rawValue, source));
		} else {
			p.changeRawValue(rawValue);
			p.setSource(source);
		}
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
	public void setValueTemporarily(EnumProperty type, String value) {
		TAB.getInstance().debug("Received API request to set property " + type + " of " + getName() + " temporarily to " + value + " by " + Thread.currentThread().getStackTrace()[2].toString());
		Property pr = getProperty(type.toString());
		if (pr == null) throw new IllegalStateException("Feature handling this property (" + type + ") is not enabled");
		pr.setTemporaryValue(value);
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx") && type.toString().contains("tag")) {
			setProperty(PropertyUtils.NAMETAG,getProperty(PropertyUtils.TAGPREFIX).getCurrentRawValue() + getProperty(PropertyUtils.CUSTOMTAGNAME).getCurrentRawValue() + getProperty(PropertyUtils.TAGSUFFIX).getCurrentRawValue(), null);
		}
		forceRefresh();
	}

	@Override
	public String getTemporaryValue(EnumProperty type) {
		Property pr = getProperty(type.toString());
		return pr == null ? null : pr.getTemporaryValue();
	}

	@Override
	public boolean hasTemporaryValue(EnumProperty type) {
		return getTemporaryValue(type) != null;
	}

	@Override
	public void removeTemporaryValue(EnumProperty type) {
		setValueTemporarily(type, null);
	}

	@Override
	public String getOriginalValue(EnumProperty type) {
		return getProperty(type.toString()).getOriginalRawValue();
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
	public String getWorldName() {
		return world;
	}

	public void setWorldName(String name) {
		world = name;
	}

	@Override
	public void sendCustomPacket(UniversalPacketPlayOut packet) {
		Object p = packet.create(getVersion());
		long time = System.nanoTime();
		sendPacket(p);
		TAB.getInstance().getCPUManager().addMethodTime("sendPacket", System.nanoTime()-time);
	}

	@Override
	public void sendCustomPacket(UniversalPacketPlayOut packet, Object feature) {
		sendCustomPacket(packet);
		TAB.getInstance().getCPUManager().packetSent(feature);
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
		if (armorStandManager == null) throw new IllegalStateException("Unlimited nametag mode is not enabled");
		if (previewingNametag) {
			armorStandManager.destroy(this);
			sendMessage(TAB.getInstance().getConfiguration().getTranslation().getString("preview-off"), true);
		} else {
			armorStandManager.spawn(this);
			sendMessage(TAB.getInstance().getConfiguration().getTranslation().getString("preview-on"), true);
		}
		previewingNametag = !previewingNametag;
	}

	@Override
	public boolean isPreviewingNametag() {
		return previewingNametag;
	}

	@Override
	public Channel getChannel() {
		return channel;
	}

	@Override
	public boolean isLoaded() {
		return onJoinFinished;
	}

	public void markAsLoaded() {
		onJoinFinished = true;
	}

	@Override
	public void setProperty(String identifier, String rawValue) {
		setProperty(identifier, rawValue, null);
	}

	@Override
	public void loadPropertyFromConfig(String property) {
		loadPropertyFromConfig(property, "");
	}

	@Override
	public void loadPropertyFromConfig(String property, String ifNotSet) {
/*		String worldGroup = TAB.getInstance().getConfiguration().getWorldGroupOf(TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("per-" + TAB.getInstance().getPlatform().getSeparatorType() + "-settings").keySet(), getWorldName());
		String value;
		Map<String, String> priorities = new LinkedHashMap<>();
		priorities.put("per-" + TAB.getInstance().getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Users." + getName() + "." + property, "Player: " + getName() + ", " + TAB.getInstance().getPlatform().getSeparatorType() + ": " + worldGroup);
		priorities.put("per-" + TAB.getInstance().getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Users." + getUniqueId().toString() + "." + property, "PlayerUUID: " + getName() + ", " + TAB.getInstance().getPlatform().getSeparatorType() + ": " + worldGroup);
		priorities.put("Users." + getName() + "." + property, "Player: " + getName());
		priorities.put("Users." + getUniqueId().toString() + "." + property, "PlayerUUID: " + getName());
		priorities.put("per-" + TAB.getInstance().getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Groups." + playerGroupFromConfig + "." + property, "Group: " + permissionGroup + ", " + TAB.getInstance().getPlatform().getSeparatorType() + ": " + worldGroup);
		priorities.put("per-" + TAB.getInstance().getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Groups._OTHER_." + property, "Group: _OTHER_," + TAB.getInstance().getPlatform().getSeparatorType() + ": " + worldGroup);
		priorities.put("Groups." + playerGroupFromConfig + "." + property, "Group: " + permissionGroup);
		priorities.put("Groups._OTHER_." + property, "Group: _OTHER_");
		for (Entry<String, String> entry : priorities.entrySet()) {
			if ((value = TAB.getInstance().getConfiguration().getConfig().getString(entry.getKey())) != null) {
				setProperty(property, value, entry.getValue());
				return;
			}
		}*/
		String value = TAB.getInstance().getConfiguration().getUsers().getProperty(getName(), property, server, world);
		if (value == null) {
			value = TAB.getInstance().getConfiguration().getUsers().getProperty(getUniqueId().toString(), property, server, world);
		}
		if (value == null) {
			value = TAB.getInstance().getConfiguration().getGroups().getProperty(getGroup().replace(".", "@#@"), property, server, world);
		}
		if (value != null) {
			setProperty(property, value, "TODO");
			return;
		}
		setProperty(property, ifNotSet, "None");
	}

	@Override
	public ArmorStandManager getArmorStandManager() {
		return armorStandManager;
	}

	@Override
	public void setArmorStandManager(ArmorStandManager armorStandManager) {
		this.armorStandManager = armorStandManager;
	}

	public void setTeamName(String name) {
		teamName = name;
	}

	@Override
	public String getTeamName() {
		if (forcedTeamName != null) return forcedTeamName;
		return teamName;
	}

	public void setTeamNameNote(String note) {
		teamNameNote = note;
	}

	@Override
	public String getTeamNameNote() {
		return teamNameNote;
	}

	public void setGroup(String permissionGroup, boolean refreshIfChanged) {
		if (this.permissionGroup.equals(permissionGroup)) return;
		if (permissionGroup != null) {
			this.permissionGroup = permissionGroup;
		} else {
			this.permissionGroup = "<null>";
			TAB.getInstance().getErrorManager().oneTimeConsoleError(TAB.getInstance().getPermissionPlugin().getName() + " v" + TAB.getInstance().getPermissionPlugin().getVersion() + " returned null permission group for " + getName());
		}
		if (refreshIfChanged) {
			forceRefresh();
		}
	}

	@Override
	public void sendPacket(Object nmsPacket, Object feature) {
		sendPacket(nmsPacket);
		TAB.getInstance().getCPUManager().packetSent(feature);
	}

	/**
	 * Returns gamemode of the player (0 for survival, 1 creative, 2 adventure, 3 spectator)
	 * @return gamemode of the player
	 */
	public abstract int getGamemode();
}