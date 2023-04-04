package me.neznamy.tab.platforms.fabric;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.player.BossBarHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;

@RequiredArgsConstructor
public class FabricBossBarHandler implements BossBarHandler {

    private final FabricTabPlayer player;
    private final Map<UUID, ServerBossEvent> bars = new HashMap<>();

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        if (bars.containsKey(id)) return;
        Component titleComponent = Component.Serializer.fromJson(IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()));
        ServerBossEvent bar = new ServerBossEvent(titleComponent, BossEvent.BossBarColor.valueOf(color.name()), BossEvent.BossBarOverlay.valueOf(style.name()));
        bars.put(id, bar);
        player.sendPacket(ClientboundBossEventPacket.createAddPacket(bar));
    }

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {
        Component titleComponent = Component.Serializer.fromJson(IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()));
        bars.get(id).setName(titleComponent);
        player.sendPacket(ClientboundBossEventPacket.createUpdateNamePacket(bars.get(id)));
    }

    @Override
    public void update(@NonNull UUID id, float progress) {
        bars.get(id).setProgress(progress);
        player.sendPacket(ClientboundBossEventPacket.createUpdateProgressPacket(bars.get(id)));
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarStyle style) {
        bars.get(id).setOverlay(BossEvent.BossBarOverlay.valueOf(style.name()));
        player.sendPacket(ClientboundBossEventPacket.createUpdateStylePacket(bars.get(id)));
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarColor color) {
        bars.get(id).setColor(BossEvent.BossBarColor.valueOf(color.name()));
        player.sendPacket(ClientboundBossEventPacket.createUpdateStylePacket(bars.get(id)));
    }

    @Override
    public void remove(@NonNull UUID id) {
        player.sendPacket(ClientboundBossEventPacket.createRemovePacket(bars.remove(id).getId()));
    }
}
