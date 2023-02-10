package me.neznamy.tab.shared.proxy.features.unlimitedtags;

import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.proxy.PluginMessageHandler;
import me.neznamy.tab.shared.proxy.ProxyPlatform;

public class ProxyArmorStandManager implements ArmorStandManager {

    private final PluginMessageHandler plm = ((ProxyPlatform) TAB.getInstance().getPlatform()).getPluginMessageHandler();
    private final NameTagX nameTagX;
    private final TabPlayer owner;

    public ProxyArmorStandManager(NameTagX nameTagX, TabPlayer owner) {
        this.nameTagX = nameTagX;
        this.owner = owner;
        owner.setProperty(nameTagX, TabConstants.Property.NAMETAG, owner.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
        for (String line : nameTagX.getDefinedLines()) {
            String text = owner.getProperty(line).get();
            plm.sendMessage(owner, "NameTagX", "SetText", line, text, IChatBaseComponent.fromColoredText(text).toString(owner.getVersion())); //rel placeholder support in the future
        }
    }

    @Override
    public void destroy() {
        plm.sendMessage(owner, "NameTagX", "Destroy");
    }

    @Override
    public void refresh(boolean force) {
        for (String line : nameTagX.getDefinedLines()) {
            if (owner.getProperty(line).update() || force) {
                String text = owner.getProperty(line).get();
                plm.sendMessage(owner, "NameTagX", "SetText", line, text, IChatBaseComponent.fromColoredText(text).toString(owner.getVersion()));
            }
        }
    }
}
