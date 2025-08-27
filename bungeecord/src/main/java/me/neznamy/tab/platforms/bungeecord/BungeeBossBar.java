package me.neznamy.tab.platforms.bungeecord;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import net.md_5.bungee.protocol.packet.BossBar;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * BossBar handler for BungeeCord. It uses packets, since
 * BungeeCord does not have a BossBar API. Only supports
 * 1.9+ players, as dealing with entities would be simply impossible.
 */
@RequiredArgsConstructor
public class BungeeBossBar extends SafeBossBar<UUID> {

    /** Player this BossBar view belongs to */
    @NotNull
    private final BungeeTabPlayer player;

    @Override
    @NotNull
    public UUID constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        return UUID.randomUUID();
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        BossBar packet = new BossBar(bar.getBossBar(), 0);
        packet.setHealth(bar.getProgress());
        packet.setTitle(player.getPlatform().transformComponent(bar.getTitle(), player.getVersion()));
        packet.setColor(bar.getColor().ordinal());
        packet.setDivision(bar.getStyle().ordinal());
        player.sendPacket(packet);
    }

    @Override
    public void updateTitle(@NotNull BossBarInfo bar) {
        BossBar packet = new BossBar(bar.getBossBar(), 3);
        packet.setTitle(player.getPlatform().transformComponent(bar.getTitle(), player.getVersion()));
        player.sendPacket(packet);
    }

    @Override
    public void updateProgress(@NotNull BossBarInfo bar) {
        BossBar packet = new BossBar(bar.getBossBar(), 2);
        packet.setHealth(bar.getProgress());
        player.sendPacket(packet);
    }

    @Override
    public void updateStyle(@NotNull BossBarInfo bar) {
        updateColor(bar);
    }

    @Override
    public void updateColor(@NotNull BossBarInfo bar) {
        BossBar packet = new BossBar(bar.getBossBar(), 4);
        packet.setDivision(bar.getStyle().ordinal());
        packet.setColor(bar.getColor().ordinal());
        player.sendPacket(packet);
    }

    @Override
    public void remove(@NotNull BossBarInfo bar) {
        player.sendPacket(new BossBar(bar.getBossBar(), 1));
    }
}
