package me.neznamy.tab.platforms.sponge8;

import lombok.Getter;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.entityview.DummyEntityView;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.platform.bossbar.AdventureBossBar;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
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

@Getter
public class SpongeTabPlayer extends BackendTabPlayer {

    @NotNull
    private final Scoreboard<SpongeTabPlayer> scoreboard = new SpongeScoreboard(this);

    @NotNull
    private final TabList tabList = new SpongeTabList(this);

    @NotNull
    private final BossBar bossBar = new AdventureBossBar(this);

    @NotNull
    private final EntityView entityView = new DummyEntityView();

    public SpongeTabPlayer(@NotNull SpongePlatform platform, @NotNull ServerPlayer player) {
        super(platform, player, player.uniqueId(), player.name(), player.world().key().value());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().connection().latency();
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        getPlayer().sendMessage(getPlatform().toComponent(message, getVersion()));
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
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public SpongePlatform getPlatform() {
        return (SpongePlatform) platform;
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
    public double getHealth() {
        return getPlayer().health().get();
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return PlainTextComponentSerializer.plainText().serialize(getPlayer().displayName().get());
    }
}
