package me.neznamy.bossbar.bungee;

import me.neznamy.bossbar.shared.SafeBossBarManager;
import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.component.KeybindComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.chat.component.TranslatableComponent;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.BossBar;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

/**
 * BossBar handler for BungeeCord. It uses packets, since
 * BungeeCord does not have a BossBar API. Only supports
 * 1.9+ players, as dealing with entities would be simply impossible.
 */
public class BungeeBossBarManager extends SafeBossBarManager<UUID> {

    /**
     * Constructs new instance for given player.
     *
     * @param   player
     *          Player this Boss bar will belong to
     */
    public BungeeBossBarManager(@NotNull ProxiedPlayer player) {
        super(player);
    }

    @Override
    @NotNull
    public UUID constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        return UUID.randomUUID();
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        sendPacket(bar, 0);
    }

    @Override
    public void updateTitle(@NotNull BossBarInfo bar) {
        sendPacket(bar, 3);
    }

    @Override
    public void updateProgress(@NotNull BossBarInfo bar) {
        sendPacket(bar, 2);
    }

    @Override
    public void updateStyle(@NotNull BossBarInfo bar) {
        sendPacket(bar, 4);
    }

    @Override
    public void updateColor(@NotNull BossBarInfo bar) {
        sendPacket(bar, 4);
    }

    @Override
    public void remove(@NotNull BossBarInfo bar) {
        sendPacket(bar, 1);
    }

    private void sendPacket(@NotNull BossBarInfo bar, int action) {
        BossBar packet = new BossBar(bar.getBossBar(), action);
        packet.setHealth(bar.getProgress());
        packet.setTitle(convert(bar.getTitle()));
        packet.setColor(bar.getColor().ordinal());
        packet.setDivision(bar.getStyle().ordinal());
        ((UserConnection)player).sendPacketQueued(packet);
    }

    @NotNull
    private BaseComponent convert(@NotNull TabComponent component) {
        if (((ProxiedPlayer)player).getPendingConnection().getVersion() >= 735) { // 1.16
            return component.convert();
        }
        return createComponent(component, false);
    }

    /**
     * Creates a bungee component using the given TAB component and modern flag for an RGB/legacy color decision.
     *
     * @param   component
     *          Component to convert
     * @param   modern
     *          {@code true} if colors should be as RGB, {@code false} if legacy
     * @return  Converted component
     */
    @NotNull
    private BaseComponent createComponent(@NotNull TabComponent component, boolean modern) {
        // TODO solve this in a better way so there is no duplication of this method among modules
        // Component type
        BaseComponent bComponent;
        if (component instanceof TextComponent) {
            bComponent = new net.md_5.bungee.api.chat.TextComponent(((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            bComponent = new net.md_5.bungee.api.chat.TranslatableComponent(((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            bComponent = new net.md_5.bungee.api.chat.KeybindComponent(((KeybindComponent) component).getKeybind());
        } else {
            throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        ChatModifier modifier = component.getModifier();
        if (modifier.getColor() != null) {
            if (modern) {
                bComponent.setColor(ChatColor.of("#" + modifier.getColor().getHexCode()));
            } else {
                bComponent.setColor(ChatColor.of(modifier.getColor().getLegacyColor().name()));
            }
        }
        bComponent.setShadowColor(modifier.getShadowColor() == null ? null : new Color(
                (modifier.getShadowColor() >> 16) & 0xFF,
                (modifier.getShadowColor() >> 8) & 0xFF,
                (modifier.getShadowColor()) & 0xFF,
                (modifier.getShadowColor() >> 24) & 0xFF
        ));
        bComponent.setBold(modifier.getBold());
        bComponent.setItalic(modifier.getItalic());
        bComponent.setObfuscated(modifier.getObfuscated());
        bComponent.setStrikethrough(modifier.getStrikethrough());
        bComponent.setUnderlined(modifier.getUnderlined());
        bComponent.setFont(modifier.getFont());

        // Extra
        for (TabComponent extra : component.getExtra()) {
            bComponent.addExtra(createComponent(extra, modern));
        }

        return bComponent;
    }
}
