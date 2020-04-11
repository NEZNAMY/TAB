package me.neznamy.tab.platforms.bukkit;

import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.ArmorStand;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class TabPlayer extends ITabPlayer{

	public Player player;

	public TabPlayer(Player p) throws Exception {
		player = p;
		world = p.getWorld().getName();
		channel = MethodAPI.getInstance().getChannel(player);
		tablistId = p.getUniqueId();
		uniqueId = p.getUniqueId();
		name = p.getName();
		int version;
		if (PluginHooks.protocolsupport){
			version = PluginHooks.ProtocolSupportAPI_getProtocolVersionId(this);
			if (version > 0) this.version = ProtocolVersion.fromNumber(version);
		} else if (PluginHooks.viaversion){
			version = PluginHooks.ViaVersion_getPlayerVersion(this);
			if (version > 0) this.version = ProtocolVersion.fromNumber(version);
		}
		nameTagVisible = !hasInvisibility();
		init();
	}
	public String getGroupFromPermPlugin() {
		if (PluginHooks.luckPerms) return PluginHooks.LuckPerms_getPrimaryGroup(this);
		if (PluginHooks.permissionsEx) {
			String[] groups = PluginHooks.PermissionsEx_getGroupNames(this);
			if (groups.length == 0) return "null";
			return groups[0];
		}
		if (PluginHooks.groupManager != null) return PluginHooks.GroupManager_getGroup(this);
		if (PluginHooks.ultrapermissions) {
			String[] groups = PluginHooks.UltraPermissions_getAllGroups(this);
			if (groups.length == 0) return "null";
			return groups[0];
		}
		if (PluginHooks.Vault_permission != null && !PluginHooks.Vault_getPermissionPlugin().equals("SuperPerms")) return PluginHooks.Vault_getPrimaryGroup(this);
		return "null";
	}
	public String[] getGroupsFromPermPlugin() {
		if (PluginHooks.luckPerms) return PluginHooks.LuckPerms_getAllGroups(this);
		if (PluginHooks.permissionsEx) return PluginHooks.PermissionsEx_getGroupNames(this);
		if (PluginHooks.groupManager != null) return PluginHooks.GroupManager_getGroups(this);
		if (PluginHooks.ultrapermissions) return PluginHooks.UltraPermissions_getAllGroups(this);
		if (PluginHooks.Vault_permission != null && !PluginHooks.Vault_getPermissionPlugin().equals("SuperPerms")) return PluginHooks.Vault_getGroups(this);
		return new String[] {"null"};
	}
	@Override
	public void setTeamVisible(boolean visible) {
		if (nameTagVisible != visible) {
			nameTagVisible = visible;
			updateTeam();
		}
	}
	public void restartArmorStands() {
		getArmorStands().forEach(a -> a.destroy());
		armorStands.clear();
		if (disabledNametag) return;
		loadArmorStands();
		for (ITabPlayer worldPlayer : Shared.getPlayers()) {
			if (this == worldPlayer) continue;
			if (!worldPlayer.getWorldName().equals(getWorldName())) continue;
			NameTagX.spawnArmorStand(this, worldPlayer);
		}
		if (previewingNametag) NameTagX.spawnArmorStand(this, this);
	}
	public void loadArmorStands() {
		armorStands.clear();
		setProperty("nametag", properties.get("tagprefix").getCurrentRawValue() + properties.get("customtagname").getCurrentRawValue() + properties.get("tagsuffix").getCurrentRawValue());
		double height = -Configs.SECRET_NTX_space;
		for (String line : Premium.dynamicLines) {
			Property p = properties.get(line);
			if (p == null || p.getCurrentRawValue().length() == 0) continue;
			String value = p.getCurrentRawValue();
			armorStands.add(new ArmorStand(this, value, height+=Configs.SECRET_NTX_space, line, false));
		}
		for (Entry<String, Double> line : Premium.staticLines.entrySet()) {
			Property p = properties.get(line.getKey());
			if (p == null || p.getCurrentRawValue().length() == 0) continue;
			String value = p.getCurrentRawValue();
			armorStands.add(new ArmorStand(this, value, line.getValue(), line.getKey(), true));
		}
		fixArmorStandHeights();
	}
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}
	public long getPing() {
		int ping = MethodAPI.getInstance().getPing(player);
		if (ping > 10000 || ping < 0) ping = -1;
		return ping;
	}
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket != null) MethodAPI.getInstance().sendPacket(player, nmsPacket);
	}
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(Placeholders.color(message));
	}
	public void sendRawMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(message);
	}
	@Override
	public boolean hasInvisibility() {
		return player.hasPotionEffect(PotionEffectType.INVISIBILITY);
	}
	@Override
	public boolean getTeamPush() {
		if (PluginHooks.libsDisguises && PluginHooks.LibsDisguises_isDisguised(this)) return false;
		if (PluginHooks.idisguise != null && PluginHooks.iDisguise_isDisguised(this)) return false; 
		return Configs.getCollisionRule(world);
	}
	@Override
	public Object getSkin() {
		return null;
	}
	@Override
	public PlayerInfoData getInfoData() {
		String name = player.getPlayerListName().equals(getName()) ? null : player.getPlayerListName();
		return new PlayerInfoData(this.name, tablistId, null, 0, EnumGamemode.CREATIVE, name);
	}
}