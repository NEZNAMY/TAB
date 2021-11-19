package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class LocationRefresher extends TabFeature {

	private final NameTagX feature;
	
	public LocationRefresher(NameTagX feature) {
		super(feature.getFeatureName(), "Processing passengers / preview");
		this.feature = feature;
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%location0%", 50, p -> {
			
			if (!feature.getVehicleManager().getPlayersInVehicle().containsKey(p) && !p.isPreviewingNametag()) return null;
			Location l = ((Player)p.getPlayer()).getLocation();
			return l.getX()+l.getY()+l.getZ(); //less cpu usage than literal Location#toString() and then .equals() when comparing last value
		});
		addUsedPlaceholders(Arrays.asList("%location0%")); //making sure it's not the same one as in WiherBossBar
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (feature.getVehicleManager().getPlayersInVehicle().containsKey(p)) {
			feature.getVehicleManager().processPassengers((Entity) p.getPlayer());
		}
		if (p.isPreviewingNametag()) {
			p.getArmorStandManager().teleport(p);
		}
	}
}