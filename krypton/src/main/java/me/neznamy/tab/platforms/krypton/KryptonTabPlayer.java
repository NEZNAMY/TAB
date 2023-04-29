package me.neznamy.tab.platforms.krypton;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.bossbar.AdventureBossBar;
import me.neznamy.tab.shared.platform.bossbar.PlatformBossBar;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.PlatformScoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kryptonmc.api.auth.ProfileProperty;
import org.kryptonmc.api.entity.player.Player;

import java.util.List;

public class KryptonTabPlayer extends TabPlayer {

    @Getter private final PlatformScoreboard<KryptonTabPlayer> scoreboard = new KryptonScoreboard(this);
    @Getter private final TabList tabList = new KryptonTabList(this);
    @Getter private final PlatformBossBar bossBar = new AdventureBossBar(getPlayer());

    public KryptonTabPlayer(Player player) {
        super(player, player.getUuid(), player.getProfile().name(), "N/A",
                player.getWorld().getName(), TAB.getInstance().getServerVersion().getNetworkId(), true);
    }

    @Override
    public boolean hasPermission(@NonNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getPing();
    }

    @Override
    public void sendMessage(@NonNull IChatBaseComponent message) {
        getPlayer().sendMessage(message.toAdventureComponent());
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return false;
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    public @Nullable TabList.Skin getSkin() {
        List<ProfileProperty> list = getPlayer().getProfile().properties();
        if (list.isEmpty()) return null;
        return new TabList.Skin(list.get(0).value(), list.get(0).signature());
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
        return false;
    }

    @Override
    public int getGamemode() {
        return getPlayer().getGameMode().ordinal();
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        getPlayer().getTabList().setHeaderAndFooter(header.toAdventureComponent(), footer.toAdventureComponent());
    }
}
