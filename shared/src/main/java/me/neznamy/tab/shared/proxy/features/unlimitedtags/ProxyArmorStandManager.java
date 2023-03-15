package me.neznamy.tab.shared.proxy.features.unlimitedtags;

import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

public class ProxyArmorStandManager implements ArmorStandManager {

    private final NameTagX nameTagX;
    private final ProxyTabPlayer owner;

    public ProxyArmorStandManager(NameTagX nameTagX, TabPlayer owner) {
        this.nameTagX = nameTagX;
        this.owner = (ProxyTabPlayer) owner;
        owner.setProperty(nameTagX, TabConstants.Property.NAMETAG, owner.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
        for (String line : nameTagX.getDefinedLines()) {
            String text = owner.getProperty(line).get();
            this.owner.sendPluginMessage("NameTagX", "SetText", line, text, IChatBaseComponent.fromColoredText(text).toString(owner.getVersion())); //rel placeholder support in the future
        }
    }

    @Override
    public void destroy() {
        owner.sendPluginMessage("NameTagX", "Destroy");
    }

    @Override
    public void refresh(boolean force) {
        for (String line : nameTagX.getDefinedLines()) {
            if (owner.getProperty(line).update() || force) {
                String text = owner.getProperty(line).get();
                owner.sendPluginMessage("NameTagX", "SetText", line, text, IChatBaseComponent.fromColoredText(text).toString(owner.getVersion()));
            }
        }
    }
}
