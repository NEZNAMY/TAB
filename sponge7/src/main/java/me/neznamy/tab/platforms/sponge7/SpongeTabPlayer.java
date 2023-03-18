package me.neznamy.tab.platforms.sponge7;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.Skin;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import org.spongepowered.api.boss.*;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SpongeTabPlayer extends ITabPlayer {

    private final Map<UUID, ServerBossBar> bossBars = new HashMap<>();

    @Getter private final Scoreboard scoreboard = new SpongeScoreboard(this);

    @Getter private final me.neznamy.tab.api.tablist.TabList tabList = new SpongeTabList(this);

    public SpongeTabPlayer(final Player player) {
        super(player, player.getUniqueId(), player.getName(), TAB.getInstance().getConfiguration().getServerName(),
                player.getWorld().getName(), ProtocolVersion.V1_12_2.getNetworkId(), true);
    }

    @Override
    public boolean hasPermission(final String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getConnection().getLatency();
    }

    @Override
    public void sendPacket(final Object packet) {
        throw new IllegalStateException("No longer supported");
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
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
    public Skin getSkin() {
        final Collection<ProfileProperty> properties = getPlayer().getProfile().getPropertyMap().get("textures");
        if (properties.isEmpty()) return null; //offline mode
        final ProfileProperty property = properties.iterator().next();
        return new Skin(property.getValue(), property.getSignature().orElse(null));
    }

    @Override
    public Player getPlayer() {
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

    @Override
    public void sendBossBar(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        ServerBossBar bar = ServerBossBar.builder()
                .name(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(title), getVersion()))
                .color(convertBossBarColor(color))
                .overlay(convertOverlay(style))
                .percent(progress)
                .build();
        bossBars.put(id, bar);
        bar.addPlayer(getPlayer());
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull String title) {
        bossBars.get(id).setName(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(title), getVersion()));
    }

    @Override
    public void updateBossBar(@NonNull UUID id, float progress) {
        bossBars.get(id).setPercent(progress);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarStyle style) {
        bossBars.get(id).setOverlay(convertOverlay(style));
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarColor color) {
        bossBars.get(id).setColor(convertBossBarColor(color));
    }

    @Override
    public void removeBossBar(@NonNull UUID id) {
        bossBars.remove(id).removePlayer(getPlayer());
    }

    private @NonNull BossBarColor convertBossBarColor(@NonNull BarColor color) {
        switch (color) {
            case PINK: return BossBarColors.PINK;
            case BLUE: return BossBarColors.BLUE;
            case RED: return BossBarColors.RED;
            case GREEN: return BossBarColors.GREEN;
            case YELLOW: return BossBarColors.YELLOW;
            case WHITE: return BossBarColors.WHITE;
            default: return BossBarColors.PURPLE;
        }
    }

    private @NonNull BossBarOverlay convertOverlay(@NonNull BarStyle style) {
        switch (style) {
            case NOTCHED_6: return BossBarOverlays.NOTCHED_6;
            case NOTCHED_10: return BossBarOverlays.NOTCHED_10;
            case NOTCHED_12: return BossBarOverlays.NOTCHED_12;
            case NOTCHED_20: return BossBarOverlays.NOTCHED_20;
            default: return BossBarOverlays.PROGRESS;
        }
    }
}
