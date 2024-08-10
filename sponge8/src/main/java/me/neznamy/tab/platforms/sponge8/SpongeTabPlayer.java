package me.neznamy.tab.platforms.sponge8;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.Collections;
import java.util.List;

/**
 * TabPlayer implementation for Sponge 8+.
 */
public class SpongeTabPlayer extends BackendTabPlayer {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   platform
     *          Server platform
     * @param   player
     *          Platform's player object
     */
    public SpongeTabPlayer(@NotNull SpongePlatform platform, @NotNull ServerPlayer player) {
        super(platform, player, player.uniqueId(), player.name(), player.world().key().value(), platform.getServerVersion().getNetworkId());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    @SneakyThrows
    public int getPing() {
        return SpongeMultiVersion.getPing.apply(getPlayer());
    }

    @Override
    public void sendMessage(@NotNull TabComponent message) {
        getPlayer().sendMessage(message.toAdventure(getVersion()));
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
    @Nullable
    public TabList.Skin getSkin() {
        List<ProfileProperty> list = getPlayer().profile().properties();
        if (list.isEmpty()) return null; // Offline mode
        return new TabList.Skin(list.get(0).value(), list.get(0).signature().orElse(null));
    }

    @Override
    @NotNull
    public ServerPlayer getPlayer() {
        return (ServerPlayer) player;
    }

    @Override
    public SpongePlatform getPlatform() {
        return (SpongePlatform) platform;
    }

    @Override
    public boolean isVanished0() {
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
    public double getHealth() {
        return getPlayer().health().get();
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return PlainTextComponentSerializer.plainText().serialize(getPlayer().displayName().get());
    }
}
