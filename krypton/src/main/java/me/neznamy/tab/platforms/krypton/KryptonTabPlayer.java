package me.neznamy.tab.platforms.krypton;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.bossbar.AdventureBossBar;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kryptonmc.api.auth.ProfileProperty;
import org.kryptonmc.api.entity.player.Player;

import java.util.List;

@Getter
public class KryptonTabPlayer extends TabPlayer {

    private final Scoreboard<KryptonTabPlayer> scoreboard = new KryptonScoreboard(this);
    private final TabList tabList = new KryptonTabList(this);
    private final BossBar bossBar = new AdventureBossBar(this);

    public KryptonTabPlayer(Player player) {
        super(player, player.getUuid(), player.getProfile().name(), TAB.getInstance().getConfiguration().getServerName(),
                player.getWorld().getName(), TAB.getInstance().getServerVersion().getNetworkId(), true);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getPing();
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        getPlayer().sendMessage(message.toAdventureComponent(getVersion()));
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
}
