package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import java.util.Collection;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FabricTabPlayer extends TabPlayer {

    @Getter private final Scoreboard<FabricTabPlayer> scoreboard = new FabricScoreboard(this);
    @Getter private final TabList tabList = new FabricTabList(this);
    @Getter private final BossBar bossBar = new FabricBossBar(this);

    public FabricTabPlayer(ServerPlayer player) {
        super(player, player.getUUID(), player.getGameProfile().getName(), TAB.getInstance().getConfiguration().getServerName(),
                "N/A", ViaVersionHook.getInstance().getPlayerVersion(player.getUUID(), player.getGameProfile().getName()), true);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return FabricTAB.getInstance().hasPermission(getPlayer().createCommandSourceStack(), permission);
    }

    @Override
    public int getPing() {
        return getPlayer().latency;
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        FabricMultiVersion.sendMessage.accept(this, message);
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
        Collection<Property> properties = getPlayer().getGameProfile().getProperties().get(TabList.TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null;
        Property skinProperty = properties.iterator().next();
        return new TabList.Skin(skinProperty.getValue(), skinProperty.getSignature());
    }

    @Override
    public @NotNull ServerPlayer getPlayer() {
        return (ServerPlayer) player;
    }

    public void sendPacket(Packet<?> packet) {
        getPlayer().connection.send(packet);
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    @Override
    public int getGamemode() {
        return getPlayer().gameMode.getGameModeForPlayer().getId();
    }
}
