package me.neznamy.tab.platforms.krypton;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.player.BossBarHandler;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.player.TabPlayer;
import me.neznamy.tab.shared.player.tablist.Skin;
import me.neznamy.tab.shared.player.tablist.TabList;
import me.neznamy.tab.shared.player.Scoreboard;
import org.kryptonmc.api.auth.ProfileProperty;
import org.kryptonmc.api.entity.player.Player;
import org.kryptonmc.krypton.entity.player.KryptonPlayer;
import org.kryptonmc.krypton.packet.Packet;

import java.util.List;

public class KryptonTabPlayer extends TabPlayer {

    @Getter private final Scoreboard<KryptonTabPlayer> scoreboard = new KryptonScoreboard(this);
    @Getter private final TabList tabList = new KryptonTabList(this);
    @Getter private final BossBarHandler bossBarHandler = new KryptonBossBarHandler(this);

    public KryptonTabPlayer(Player player, int protocolVersion) {
        super(player, player.getUuid(), player.getProfile().name(), "N/A", player.getWorld().getName(), protocolVersion, true);
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getConnection().latency();
    }

    public void sendPacket(Object packet) {
        getPlayer().getConnection().send((Packet) packet);
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
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
    public Skin getSkin() {
        List<ProfileProperty> list = getPlayer().getProfile().properties();
        if (list.isEmpty()) return null;
        return new Skin(list.get(0).value(), list.get(0).signature());
    }

    @Override
    public KryptonPlayer getPlayer() {
        return (KryptonPlayer) player;
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
        getPlayer().sendPlayerListHeaderAndFooter(header.toAdventureComponent(), footer.toAdventureComponent());
    }
}
