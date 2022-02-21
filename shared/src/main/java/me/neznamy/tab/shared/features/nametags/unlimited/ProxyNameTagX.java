package me.neznamy.tab.shared.features.nametags.unlimited;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.proxy.PluginMessageHandler;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

public class ProxyNameTagX extends NameTagX {

    private final PluginMessageHandler plm;

    public ProxyNameTagX(PluginMessageHandler plm) {
        super(ProxyArmorStandManager::new);
        this.plm = plm;
    }

    @Override
    public void onServerChange(TabPlayer p, String from, String to) {
        super.onServerChange(p, from, to);
        for (String line : getDefinedLines()) {
            String text = p.getProperty(line).get();
            plm.sendMessage(p, "NameTagX", "SetText", line, text, IChatBaseComponent.fromColoredText(text).toString(p.getVersion())); //rel placeholder support in the future
        }
    }

    @Override
    public boolean isOnBoat(TabPlayer player) {
        return ((ProxyTabPlayer)player).isOnBoat();
    }

    @Override
    public void setNameTagPreview(TabPlayer player, boolean status) {
        plm.sendMessage(player, "NameTagX", "Preview", status);
    }

    @Override
    public void resumeArmorStands(TabPlayer player) {
        plm.sendMessage(player, "NameTagX", "Resume");
    }

    @Override
    public void pauseArmorStands(TabPlayer player) {
        plm.sendMessage(player, "NameTagX", "Pause");
    }

    @Override
    public void updateNameTagVisibilityView(TabPlayer player) {
        plm.sendMessage(player, "NameTagX", "VisibilityView");
    }
}
