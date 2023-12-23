package me.neznamy.tab.shared.proxy.features.unlimitedtags;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.nametags.unlimited.ArmorStandManager;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.nametags.Destroy;
import me.neznamy.tab.shared.proxy.message.outgoing.nametags.SetText;
import org.jetbrains.annotations.NotNull;

/**
 * Armor stand manager for proxies.
 */
public class ProxyArmorStandManager implements ArmorStandManager {

    private final NameTagX nameTagX;
    private final ProxyTabPlayer owner;

    /**
     * Constructs new instance and loads lines.
     *
     * @param   nameTagX
     *          Main feature
     * @param   owner
     *          Owner of the armor stand manager
     */
    public ProxyArmorStandManager(@NotNull NameTagX nameTagX, @NotNull TabPlayer owner) {
        this.nameTagX = nameTagX;
        this.owner = (ProxyTabPlayer) owner;
        owner.setProperty(nameTagX, TabConstants.Property.NAMETAG, owner.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
        for (String line : nameTagX.getDefinedLines()) {
            String text = owner.getProperty(line).get();
            this.owner.sendPluginMessage(new SetText(line, text));
        }
    }

    @Override
    public void destroy() {
        owner.sendPluginMessage(new Destroy());
    }

    @Override
    public void refresh(boolean force) {
        for (String line : nameTagX.getDefinedLines()) {
            if (owner.getProperty(line).update() || force) {
                String text = owner.getProperty(line).get();
                owner.sendPluginMessage(new SetText(line, text));
            }
        }
    }
}
