package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.packet.PlayerListItem;

/**
 * Main class for BungeeCord.
 */
public class BungeeTAB extends Plugin {

    @Override
    public void onEnable() {
        try {
            PlayerListItem.Item.class.getDeclaredField("listOrder");
            TAB.create(new BungeePlatform(this));
        } catch (NoSuchFieldException e) {
            getLogger().warning(EnumChatFormat.RED + "The plugin requires BungeeCord build #1861 " +
                    "(released on August 24th, 2024) and up (or an equivalent fork) to work. If you are using a fork that did not" +
                    " update to the new BungeeCord version yet, stay on an older TAB version made for older BungeeCord builds in the meantime.");
        }
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
}