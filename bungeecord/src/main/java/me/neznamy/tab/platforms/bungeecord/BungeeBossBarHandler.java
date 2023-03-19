package me.neznamy.tab.platforms.bungeecord;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import net.md_5.bungee.protocol.packet.BossBar;

import java.util.UUID;

@RequiredArgsConstructor
public class BungeeBossBarHandler implements BossBarHandler {
    
    private final BungeeTabPlayer player;

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        if (player.getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 0);
        bossbar.setHealth(progress);
        bossbar.setTitle(IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()));
        bossbar.setColor(color.ordinal());
        bossbar.setDivision(style.ordinal());
        player.getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {
        if (player.getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 3);
        bossbar.setTitle(IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()));
        player.getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void update(@NonNull UUID id, float progress) {
        if (player.getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 2);
        bossbar.setHealth(progress);
        player.getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarStyle style) {
        if (player.getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 4);
        bossbar.setDivision(style.ordinal());
        player.getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarColor color) {
        if (player.getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 4);
        bossbar.setDivision(color.ordinal());
        player.getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void remove(@NonNull UUID id) {
        if (player.getVersion().getMinorVersion() < 9) return;
        player.getPlayer().unsafe().sendPacket(new BossBar(id, 1));
    }
}
