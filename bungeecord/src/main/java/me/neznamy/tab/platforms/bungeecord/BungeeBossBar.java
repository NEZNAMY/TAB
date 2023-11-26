package me.neznamy.tab.platforms.bungeecord;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BossBar handler for BungeeCord. It uses packets, since
 * BungeeCord does not have a BossBar API. Only supports
 * 1.9+ players, as dealing with entities would be simply impossible.
 */
@RequiredArgsConstructor
public class BungeeBossBar implements BossBar {

    @NotNull
    private final BungeeTabPlayer player;

    /** Both are included in the same action, need to remember and write them both */
    @NotNull
    private final Map<UUID, Integer> colors = new HashMap<>();

    @NotNull
    private final Map<UUID, Integer> styles = new HashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar packet = new net.md_5.bungee.protocol.packet.BossBar(id, 0);
        packet.setHealth(progress);
        packet.setTitle(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()));
        packet.setColor(color.ordinal());
        packet.setDivision(style.ordinal());
        player.sendPacket(packet);

        colors.put(id, color.ordinal());
        styles.put(id, style.ordinal());
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar packet = new net.md_5.bungee.protocol.packet.BossBar(id, 3);
        packet.setTitle(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()));
        player.sendPacket(packet);
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar packet = new net.md_5.bungee.protocol.packet.BossBar(id, 2);
        packet.setHealth(progress);
        player.sendPacket(packet);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar packet = new net.md_5.bungee.protocol.packet.BossBar(id, 4);
        packet.setDivision(style.ordinal());
        packet.setColor(colors.get(id));
        player.sendPacket(packet);

        styles.put(id, style.ordinal());
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar packet = new net.md_5.bungee.protocol.packet.BossBar(id, 4);
        packet.setDivision(styles.get(id));
        packet.setColor(color.ordinal());
        player.sendPacket(packet);

        colors.put(id, color.ordinal());
    }

    @Override
    public void remove(@NotNull UUID id) {
        if (player.getVersion().getMinorVersion() < 9) return;

        player.sendPacket(new net.md_5.bungee.protocol.packet.BossBar(id, 1));

        colors.remove(id);
        styles.remove(id);
    }
}
