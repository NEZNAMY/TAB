package me.neznamy.tab.platforms.sponge;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component;
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

    public SpongeTabPlayer(ServerPlayer player) {
        super(player, player.uniqueId(), player.name(), TAB.getInstance().getConfiguration().getServerName(),
                player.world().key().value(),
                ProtocolVersion.V1_16_5.getNetworkId(), true);
        //TODO initialize channel
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
        if (packet instanceof PacketPlayOutBoss) {
            handle((PacketPlayOutBoss) packet);
        } else if (packet instanceof PacketPlayOutPlayerListHeaderFooter) {
            handle((PacketPlayOutPlayerListHeaderFooter)packet);
        } else {
            throw new UnsupportedOperationException("Not implemented yet"); // send packet
        }
    }

    private void handle(PacketPlayOutBoss packet) {
        throw new UnsupportedOperationException("Not implemented yet");
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
}
