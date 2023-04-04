package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import java.util.Collection;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.player.BossBarHandler;
import me.neznamy.tab.shared.player.Scoreboard;
import me.neznamy.tab.shared.player.TabPlayer;
import me.neznamy.tab.shared.player.tablist.Skin;
import me.neznamy.tab.shared.player.tablist.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;

public class FabricTabPlayer extends TabPlayer {

    private static final boolean viaVersion = ReflectionUtils.classExists("com.viaversion.viaversion.api.Via");

    @Getter private final Scoreboard<FabricTabPlayer> scoreboard = new FabricScoreboard(this);
    @Getter private final TabList tabList = new FabricTabList(this);
    @Getter private final BossBarHandler bossBarHandler = new FabricBossBarHandler(this);

    public FabricTabPlayer(ServerPlayer player) {
        super(player, player.getUUID(), player.getGameProfile().getName(), TAB.getInstance().getConfiguration().getServerName(),
                player.getLevel().dimension().location().toString(), getProtocolVersion(player), true);
    }

    private static int getProtocolVersion(ServerPlayer player) {
        if (viaVersion) {
            return ProtocolVersion.getPlayerVersionVia(player.getUUID(), player.getGameProfile().getName());
        }
        return TAB.getInstance().getServerVersion().getNetworkId();
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public int getPing() {
        return getPlayer().latency;
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendSystemMessage(FabricTAB.toComponent(message, getVersion()));
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
        Collection<Property> properties = getPlayer().getGameProfile().getProperties().get(TabList.TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null;
        Property skinProperty = properties.iterator().next();
        return new Skin(skinProperty.getValue(), skinProperty.getSignature());
    }

    @Override
    public ServerPlayer getPlayer() {
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

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        getPlayer().connection.send(new ClientboundTabListPacket(
                FabricTAB.toComponent(header, getVersion()),
                FabricTAB.toComponent(footer, getVersion()))
        );
    }
}
