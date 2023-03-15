package me.neznamy.tab.platforms.sponge8;

import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.*;

public final class SpongeTabPlayer extends ITabPlayer {

    private static final ComponentCache<IChatBaseComponent, Component> adventureCache = new ComponentCache<>(10000,
            (component, clientVersion) -> GsonComponentSerializer.gson().deserialize(component.toString(clientVersion)));

    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public SpongeTabPlayer(ServerPlayer player) {
        super(player, player.uniqueId(), player.name(), TAB.getInstance().getConfiguration().getServerName(),
                player.world().key().value(), getProtocolVersion(player), true);
    }

    private static int getProtocolVersion(ServerPlayer player) {
        if (Sponge.pluginManager().plugin(TabConstants.Plugin.VIAVERSION.toLowerCase()).isPresent()) {
            return ProtocolVersion.getPlayerVersionVia(player.uniqueId(), player.name());
        }
        return ProtocolVersion.V1_16_5.getNetworkId();
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
        ((net.minecraft.server.level.ServerPlayer) getPlayer()).connection.send((Packet<?>) packet);
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendMessage(adventureCache.get(message, getVersion()));
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

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        getPlayer().tabList().setHeaderAndFooter(adventureCache.get(header, version), adventureCache.get(footer, version));
    }

    @Override
    public void sendBossBar(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        if (bossBars.containsKey(id)) return;
        BossBar bar = BossBar.bossBar(adventureCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()),
                progress, BossBar.Color.valueOf(color.toString()), BossBar.Overlay.valueOf(style.toString()));
        bossBars.put(id, bar);
        getPlayer().showBossBar(bar);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull String title) {
        bossBars.get(id).name(adventureCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()));
    }

    @Override
    public void updateBossBar(@NonNull UUID id, float progress) {
        bossBars.get(id).progress(progress);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarStyle style) {
        bossBars.get(id).overlay(BossBar.Overlay.valueOf(style.toString()));
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarColor color) {
        bossBars.get(id).color(BossBar.Color.valueOf(color.toString()));
    }

    @Override
    public void removeBossBar(@NonNull UUID id) {
        getPlayer().hideBossBar(bossBars.remove(id));
    }

    @Override
    public void setObjectiveDisplaySlot(int slot, @NonNull String objective) {
        sendPacket(new ClientboundSetDisplayObjectivePacket(slot,
                new Objective(new Scoreboard(), objective, null, TextComponent.EMPTY, null)));
    }

    @Override
    public void setScoreboardScore0(@NonNull String objective, @NonNull String player, int score) {
        sendPacket(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective, player, score));
    }

    @Override
    public void removeScoreboardScore0(@NonNull String objective, @NonNull String player) {
        sendPacket(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective, player, 0));
    }

    public void setPlayer(final ServerPlayer player) {
        this.player = player;
    }
}
