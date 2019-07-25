package me.neznamy.tab.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.bukkit.NameTagLineManager;
import me.neznamy.tab.bukkit.objects.ArmorStand;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.HeaderFooter;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.ServerType;
import me.neznamy.tab.shared.TabCommand;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

public class TABAPI {

	public static List<UUID> hiddenNametag = new ArrayList<UUID>();

	public static ArmorStand bindLine(UUID uniqueId, String text, double heightDifference){
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (!Configs.unlimitedTags) throw new IllegalStateException("Unlimited nametag mode is not enabled");
		return NameTagLineManager.bindLine(t, text, heightDifference, (Math.random()*1000000)+"");
	}
	public static void unbindLine(UUID uniqueId, ArmorStand as) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (!Configs.unlimitedTags) throw new IllegalStateException("Unlimited nametag mode is not enabled");
		as.destroy();
		t.armorStands.remove(as);
	}
	public static boolean isUnlimitedNameTagModeEnabled() {
		return Configs.unlimitedTags;
	}
	public static void enableUnlimitedNameTagModePermanently() {
		if (Shared.servertype == ServerType.BUKKIT) {
			Configs.config.set("change-nametag-prefix-suffix", true);
			Configs.config.set("unlimited-nametag-prefix-suffix-mode.enabled", true);
			Configs.config.save();
			me.neznamy.tab.bukkit.Main.instance.unload();
			me.neznamy.tab.bukkit.Main.instance.load(false, false);
		}
	}
	public static void setCustomTabNameTemporarily(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		t.temporaryCustomTabName = value;
		if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
		t.updatePlayerListName(false);
	}
	public static void setCustomTagNameTemporarily(UUID uniqueId, String value) {
		if (!Configs.unlimitedTags) return;//throw new IllegalStateException("Unlimited nametag mode is not enabled! Use TABAPI.enableUnlimitedNameTagModePermanently() to enable it (you can also enable it in the config).");
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		t.temporaryCustomTagName = value;
		if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
		t.updatePlayerListName(false);
	}
	public static void setTabPrefixTemporarily(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		t.temporaryTabPrefix = value;
		t.updatePlayerListName(false);
	}
	public static void setTabSuffixTemporarily(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		t.temporaryTabSuffix = value;
		t.updatePlayerListName(false);
	}
	public static void setTagPrefixTemporarily(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		t.temporaryTagPrefix = value;
		if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
	}
	public static void setTagSuffixTemporarily(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		t.temporaryTagSuffix = value;
		if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
	}
	
	public static void setCustomTabNamePermanently(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		TabCommand.savePlayer(null, Shared.getPlayer(uniqueId).getName(), "customtabname", value);
		t.updatePlayerListName(false);
		if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
	}
	public static void setCustomTagNamePermanently(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		if (!Configs.unlimitedTags) return;//throw new IllegalStateException("Unlimited nametag mode is not enabled! Use TABAPI.enableUnlimitedNameTagModePermanently() to enable it (you can also enable it in the config).");
		TabCommand.savePlayer(null, Shared.getPlayer(uniqueId).getName(), "customtagname", value);
		t.updatePlayerListName(false);
		if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
	}
	public static void setTabPrefixPermanently(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		TabCommand.savePlayer(null, Shared.getPlayer(uniqueId).getName(), "tabprefix", value);
		t.updatePlayerListName(false);
	}
	public static void setTabSuffixPermanently(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		TabCommand.savePlayer(null, Shared.getPlayer(uniqueId).getName(), "tabsuffix", value);
		t.updatePlayerListName(false);
	}
	public static void setTagPrefixPermanently(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		TabCommand.savePlayer(null, Shared.getPlayer(uniqueId).getName(), "tagprefix", value);
		if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
	}
	public static void setTagSuffixPermanently(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t == null) return;
		TabCommand.savePlayer(null, Shared.getPlayer(uniqueId).getName(), "tagsuffix", value);
		if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
	}
	
	public static String getTemporaryCustomTabName(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).temporaryCustomTabName;
	}
	public static String getTemporaryCustomTagName(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).temporaryCustomTagName;
	}
	public static String getTemporaryTabPrefix(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).temporaryTabPrefix;
	}
	public static String getTemporaryTabSuffix(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).temporaryTabSuffix;
	}
	public static String getTemporaryTagPrefix(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).temporaryTagPrefix;
	}
	public static String getTemporaryTagSuffix(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).temporaryTagSuffix;
	}
	
	public static boolean hasTemporaryCustomTabName(UUID uniqueId) {
		return getTemporaryCustomTabName(uniqueId) != null;
	}
	public static boolean hasTemporaryCustomTagName(UUID uniqueId) {
		return getTemporaryCustomTagName(uniqueId) != null;
	}
	public static boolean hasTemporaryTabPrefix(UUID uniqueId) {
		return getTemporaryTabPrefix(uniqueId) != null;
	}
	public static boolean hasTemporaryTabSuffix(UUID uniqueId) {
		return getTemporaryTabSuffix(uniqueId) != null;
	}
	public static boolean hasTemporaryTagPrefix(UUID uniqueId) {
		return getTemporaryTagPrefix(uniqueId) != null;
	}
	public static boolean hasTemporaryTagSuffix(UUID uniqueId) {
		return getTemporaryTagSuffix(uniqueId) != null;
	}
	
	public static void removeTemporaryCustomTabName(UUID uniqueId) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t != null) {
			t.temporaryCustomTabName = null;
			t.updatePlayerListName(false);
		}
	}
	public static void removeTemporaryCustomTagName(UUID uniqueId) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t != null) {
			t.temporaryCustomTagName = null;
			t.updatePlayerListName(false);
			if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
		}
	}
	public static void removeTemporaryTabPrefix(UUID uniqueId) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t != null) {
			t.temporaryTabPrefix = null;
			t.updatePlayerListName(false);
		}
	}
	public static void removeTemporaryTabSuffix(UUID uniqueId) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t != null) {
			t.temporaryTabSuffix = null;
			t.updatePlayerListName(false);
		}
	}
	public static void removeTemporaryTagPrefix(UUID uniqueId) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t != null) {
			t.temporaryTagPrefix = null;
			if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
		}
	}
	public static void removeTemporaryTagSuffix(UUID uniqueId) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (t != null) {
			t.temporaryTagSuffix = null;
			if (NameTag16.enable || Configs.unlimitedTags) t.updateTeam();
		}
	}
	
	public static void setAboveNameTemporarily(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (!Configs.unlimitedTags) return;//throw new IllegalStateException("Unlimited nametag mode is not enabled! Use TABAPI.enableUnlimitedNameTagModePermanently() to enable it (you can also enable it in the config).");
		t.temporaryAboveName = value;
		t.restartArmorStands();
	}
	public static void setBelowNameTemporarily(UUID uniqueId, String value) {
		ITabPlayer t = Shared.getPlayer(uniqueId);
		if (!Configs.unlimitedTags) return;//throw new IllegalStateException("Unlimited nametag mode is not enabled! Use TABAPI.enableUnlimitedNameTagModePermanently() to enable it (you can also enable it in the config).");
		t.temporaryBelowName = value;
		t.restartArmorStands();
	}
	public static void sendHeaderFooter(UUID uniqueId, String header, String footer) {
		new PacketPlayOutPlayerListHeaderFooter(header, footer).send(Shared.getPlayer(uniqueId));
	}
	public static void refreshHeaderFooter(UUID uniqueId) {
		HeaderFooter.refreshHeaderFooter(Shared.getPlayer(uniqueId));
	}
	public static void clearHeaderFooter(UUID uniqueId) {
		new PacketPlayOutPlayerListHeaderFooter("","").send(Shared.getPlayer(uniqueId));
	}
	public static String getOriginalTabPrefix(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).tabPrefix;
	}
	public static String getOriginalTagPrefix(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).tagPrefix;
	}
	public static String getOriginalTabSuffix(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).tabSuffix;
	}
	public static String getOriginalTagSuffix(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).tagSuffix;
	}
	public static String getOriginalCustomTagName(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).customtagname;
	}
	public static String getOriginalCustomTabName(UUID uniqueId) {
		return Shared.getPlayer(uniqueId).customtabname;
	}
	public static void hideNametag(UUID uniqueId) {
		hiddenNametag.add(uniqueId);
		Shared.getPlayer(uniqueId).updateTeamPrefixSuffix();
	}
	public static void showNametag(UUID uniqueId) {
		hiddenNametag.remove(uniqueId);
		Shared.getPlayer(uniqueId).updateTeamPrefixSuffix();
	}
	public static boolean hasHiddenNametag(UUID uniqueId) {
		return hiddenNametag.contains(uniqueId);
	}
}