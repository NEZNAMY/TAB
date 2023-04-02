package me.neznamy.tab.platforms.sponge8;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.player.BossBarHandler;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.player.TabPlayer;
import me.neznamy.tab.shared.player.tablist.Skin;
import me.neznamy.tab.shared.player.tablist.TabList;
import me.neznamy.tab.shared.player.Scoreboard;
import me.neznamy.tab.shared.TAB;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.Collections;
import java.util.List;

public final class SpongeTabPlayer extends TabPlayer {

    @Getter private final Scoreboard<SpongeTabPlayer> scoreboard = new SpongeScoreboard(this);
    @Getter private final TabList tabList = new SpongeTabList(this);
    @Getter private final BossBarHandler bossBarHandler = new SpongeBossBarHandler(this);

    public SpongeTabPlayer(ServerPlayer player) {
        super(player, player.uniqueId(), player.name(), TAB.getInstance().getConfiguration().getServerName(),
                player.world().key().value(), getProtocolVersion(player), true);
    }

    private static int getProtocolVersion(ServerPlayer player) {
        if (Sponge.pluginManager().plugin(TabConstants.Plugin.VIAVERSION.toLowerCase()).isPresent()) {
            return ProtocolVersion.getPlayerVersionVia(player.uniqueId(), player.name());
        }
        return TAB.getInstance().getServerVersion().getNetworkId();
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
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendMessage(Sponge8TAB.getAdventureCache().get(message, getVersion()));
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
        getPlayer().tabList().setHeaderAndFooter(Sponge8TAB.getAdventureCache().get(header, version), Sponge8TAB.getAdventureCache().get(footer, version));
    }

    public void setPlayer(final ServerPlayer player) {
        this.player = player;
    }
}
