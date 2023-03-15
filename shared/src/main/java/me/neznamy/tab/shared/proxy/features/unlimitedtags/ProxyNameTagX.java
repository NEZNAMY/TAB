package me.neznamy.tab.shared.proxy.features.unlimitedtags;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

public class ProxyNameTagX extends NameTagX {

    public ProxyNameTagX() {
        super(ProxyArmorStandManager::new);
    }

    @Override
    public void onServerChange(TabPlayer p, String from, String to) {
        super.onServerChange(p, from, to);
        if (isPreviewingNametag(p)) {
            ((ProxyTabPlayer)p).sendPluginMessage("NameTagX", "Preview", true);
        }
        for (String line : getDefinedLines()) {
            String text = p.getProperty(line).get();
            ((ProxyTabPlayer)p).sendPluginMessage("NameTagX", "SetText", line, text, IChatBaseComponent.fromColoredText(text).toString(p.getVersion())); //rel placeholder support in the future
        }
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        super.onQuit(disconnectedPlayer);
        armorStandManagerMap.remove(disconnectedPlayer); // WeakHashMap doesn't clear this due to value referencing the key
    }

    @Override
    public boolean isOnBoat(TabPlayer player) {
        return ((ProxyTabPlayer)player).isOnBoat();
    }

    @Override
    public void setNameTagPreview(TabPlayer player, boolean status) {
        ((ProxyTabPlayer)player).sendPluginMessage("NameTagX", "Preview", status);
    }

    @Override
    public void resumeArmorStands(TabPlayer player) {
        ((ProxyTabPlayer)player).sendPluginMessage("NameTagX", "Resume");
    }

    @Override
    public void pauseArmorStands(TabPlayer player) {
        ((ProxyTabPlayer)player).sendPluginMessage("NameTagX", "Pause");
    }

    @Override
    public void updateNameTagVisibilityView(TabPlayer player) {
        ((ProxyTabPlayer)player).sendPluginMessage("NameTagX", "VisibilityView");
    }
}
