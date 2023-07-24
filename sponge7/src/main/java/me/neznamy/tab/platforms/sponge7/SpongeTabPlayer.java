package me.neznamy.tab.platforms.sponge7;

import lombok.Getter;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.entityview.DummyEntityView;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;

import java.util.Collection;

@Getter
public class SpongeTabPlayer extends BackendTabPlayer {

    private final Scoreboard<SpongeTabPlayer> scoreboard = new SpongeScoreboard(this);
    private final TabList tabList = new SpongeTabList(this);
    private final BossBar bossBar = new SpongeBossBar(this);
    private final EntityView entityView = new DummyEntityView();

    public SpongeTabPlayer(final Player player) {
        super(player, player.getUniqueId(), player.getName(), player.getWorld().getName());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getConnection().getLatency();
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        getPlayer().sendMessage(Text.of(message.toLegacyText()));
    }

    @Override
    public boolean hasInvisibilityPotion() {
        PotionEffectData potionEffects = getPlayer().get(PotionEffectData.class).orElse(null);
        if (potionEffects == null) return false;
        return potionEffects.asList().stream().anyMatch(effect -> effect.getType().equals(PotionEffectTypes.INVISIBILITY));
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    public TabList.Skin getSkin() {
        Collection<ProfileProperty> properties = getPlayer().getProfile().getPropertyMap().get(TabList.TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null; // Offline mode
        ProfileProperty property = properties.iterator().next();
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
        GameMode gameMode = getPlayer().getGameModeData().type().get();
        if (gameMode.equals(GameModes.CREATIVE)) return 1;
        if (gameMode.equals(GameModes.ADVENTURE)) return 2;
        if (gameMode.equals(GameModes.SPECTATOR)) return 3;
        return 0;
    }

    @Override
    public double getHealth() {
        return getPlayer().health().get();
    }

    @Override
    public String getDisplayName() {
        return getPlayer().getDisplayNameData().displayName().get().toPlain();
    }
}
