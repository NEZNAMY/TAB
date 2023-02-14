package me.neznamy.tab.platforms.sponge8;

import io.netty.channel.Channel;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.Collections;
import java.util.List;

public final class SpongeTabPlayer extends ITabPlayer {

    private static final ComponentCache<IChatBaseComponent, Component> textCache = new ComponentCache<>(10000,
            (component, version) -> Component.Serializer.fromJson(component.toString(version)));

    private static final ComponentCache<IChatBaseComponent, net.kyori.adventure.text.Component> adventureCache = new ComponentCache<>(10000,
            (component, clientVersion) -> GsonComponentSerializer.gson().deserialize(component.toString(clientVersion)));

    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public SpongeTabPlayer(ServerPlayer player) {
        super(player, player.uniqueId(), player.name(), TAB.getInstance().getConfiguration().getServerName(),
                player.world().key().value(),
                ProtocolVersion.V1_16_5.getNetworkId(), true);

        try {
            final Field channelField = Connection.class.getDeclaredField("channel");
            channelField.setAccessible(true);
            channel = (Channel) channelField.get(((net.minecraft.server.level.ServerPlayer) player).connection.connection);
        } catch (final ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().connection().latency();
    }

    @Override
    public void sendPacket(Object packet) {
        if (packet == null) return;
        if (packet instanceof Packet<?>) {
            ((net.minecraft.server.level.ServerPlayer) getPlayer()).connection.send((Packet<?>) packet);
            return;
        }
        if (packet instanceof PacketPlayOutBoss) {
            handle((PacketPlayOutBoss) packet);
        } else if (packet instanceof PacketPlayOutPlayerListHeaderFooter) {
            handle((PacketPlayOutPlayerListHeaderFooter)packet);
        }
    }

    private void handle(PacketPlayOutBoss packet) {
        BossBar bar;
        switch (packet.getAction()) {
            case ADD:
                if (bossBars.containsKey(packet.getId())) return;
                bar = BossBar.bossBar(adventureCache.get(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()), packet.getPct(),
                        BossBar.Color.valueOf(packet.getColor().toString()), BossBar.Overlay.valueOf(packet.getOverlay().toString()));
                if (packet.isCreateWorldFog()) bar.addFlag(BossBar.Flag.CREATE_WORLD_FOG);
                if (packet.isDarkenScreen()) bar.addFlag(BossBar.Flag.DARKEN_SCREEN);
                if (packet.isPlayMusic()) bar.addFlag(BossBar.Flag.PLAY_BOSS_MUSIC);
                bossBars.put(packet.getId(), bar);
                getPlayer().showBossBar(bar);
                break;
            case REMOVE:
                bar = bossBars.remove(packet.getId());
                if (bar != null) getPlayer().hideBossBar(bar);
                break;
            case UPDATE_PCT:
                bar = bossBars.get(packet.getId());
                if (bar != null) bar.progress(packet.getPct());
                break;
            case UPDATE_NAME:
                bar = bossBars.get(packet.getId());
                if (bar != null) bar.name(adventureCache.get(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()));
                break;
            case UPDATE_STYLE:
                bar = bossBars.get(packet.getId());
                if (bar != null) {
                    bar.color(BossBar.Color.valueOf(packet.getColor().toString()));
                    bar.overlay(BossBar.Overlay.valueOf(packet.getOverlay().toString()));
                }
                break;
            case UPDATE_PROPERTIES:
                bar = bossBars.get(packet.getId());
                if (bar != null) {
                    processFlag(bar, packet.isCreateWorldFog(), BossBar.Flag.CREATE_WORLD_FOG);
                    processFlag(bar, packet.isDarkenScreen(), BossBar.Flag.DARKEN_SCREEN);
                    processFlag(bar, packet.isPlayMusic(), BossBar.Flag.PLAY_BOSS_MUSIC);
                }
                break;
        }
    }

    private void processFlag(final BossBar bar, final boolean targetValue, final BossBar.Flag flag) {
        if (targetValue) {
            if (!bar.hasFlag(flag)) bar.addFlag(flag);
        } else {
            if (bar.hasFlag(flag)) bar.removeFlag(flag);
        }
    }

    private void handle(PacketPlayOutPlayerListHeaderFooter packet) {
        getPlayer().tabList().setHeaderAndFooter(
                adventureCache.get(packet.getHeader(), getVersion()),
                adventureCache.get(packet.getFooter(), getVersion())
        );
    }

    @Override
    public boolean hasInvisibilityPotion() {
        for (PotionEffect effect : getPlayer().get(Keys.POTION_EFFECTS).orElse(Collections.emptyList())) {
            if (effect.type() == PotionEffectTypes.INVISIBILITY.get()) return true;
        }
        return false;
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    public Skin getSkin() {
        List<ProfileProperty> list = getPlayer().profile().properties();
        if (list.isEmpty()) return null;
        return new Skin(list.get(0).value(), list.get(0).signature().orElse(null));
    }

    @Override
    public ServerPlayer getPlayer() {
        return (ServerPlayer) player;
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public boolean isVanished() {
        return getPlayer().vanishState().get().invisible();
    }

    @Override
    public int getGamemode() {
        if (getPlayer().gameMode().get() == GameModes.CREATIVE.get()) return 1;
        if (getPlayer().gameMode().get() == GameModes.ADVENTURE.get()) return 2;
        if (getPlayer().gameMode().get() == GameModes.SPECTATOR.get()) return 3;
        return 0;
    }

    public void setPlayer(final ServerPlayer player) {
        this.player = player;
    }
}
