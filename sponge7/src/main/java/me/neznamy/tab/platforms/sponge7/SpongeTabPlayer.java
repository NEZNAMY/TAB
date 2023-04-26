package me.neznamy.tab.platforms.sponge7;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.bossbar.PlatformBossBar;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.tablist.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.PlatformScoreboard;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.Collection;

public final class SpongeTabPlayer extends TabPlayer {

    @Getter private final PlatformScoreboard<SpongeTabPlayer> scoreboard = new SpongeScoreboard(this);
    @Getter private final TabList tabList = new SpongeTabList(this);
    @Getter private final PlatformBossBar bossBar = new SpongeBossBar(this);

    public SpongeTabPlayer(final Player player) {
        super(player, player.getUniqueId(), player.getName(), TAB.getInstance().getConfiguration().getServerName(),
                player.getWorld().getName(), TAB.getInstance().getServerVersion().getNetworkId(), true);
    }

    @Override
    public boolean hasPermission(final @NonNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getConnection().getLatency();
    }

    @Override
    public void sendMessage(@NonNull IChatBaseComponent message) {
        getPlayer().sendMessage(Sponge7TAB.getTextCache().get(message, getVersion()));
    }

    @Override
    public boolean hasInvisibilityPotion() {
        final PotionEffectData potionEffects = getPlayer().get(PotionEffectData.class).orElse(null);
        if (potionEffects == null) return false;
        return potionEffects.asList().stream().anyMatch(effect -> effect.getType().equals(PotionEffectTypes.INVISIBILITY));
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    public TabList.Skin getSkin() {
        final Collection<ProfileProperty> properties = getPlayer().getProfile().getPropertyMap().get(TabList.TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null; //offline mode
        final ProfileProperty property = properties.iterator().next();
        return new TabList.Skin(property.getValue(), property.getSignature().orElse(null));
    }

    @Override
    public @NotNull Player getPlayer() {
        return (Player) player;
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public boolean isVanished() {
        return getPlayer().get(Keys.VANISH).orElse(false);
    }

    @Override
    public int getGamemode() {
        final GameMode gameMode = getPlayer().getGameModeData().type().get();
        if (gameMode.equals(GameModes.CREATIVE)) return 1;
        if (gameMode.equals(GameModes.ADVENTURE)) return 2;
        if (gameMode.equals(GameModes.SPECTATOR)) return 3;
        return 0;
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        getPlayer().getTabList().setHeaderAndFooter(Sponge7TAB.getTextCache().get(header, version), Sponge7TAB.getTextCache().get(footer, version));
    }
}
