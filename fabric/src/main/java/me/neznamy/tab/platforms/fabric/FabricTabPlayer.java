package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import lombok.Getter;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Getter
public class FabricTabPlayer extends BackendTabPlayer {

    private final Scoreboard<FabricTabPlayer> scoreboard = new FabricScoreboard(this);
    private final TabList tabList = new FabricTabList(this);
    private final BossBar bossBar = new FabricBossBar(this);
    private final EntityView entityView = new FabricEntityView(this);

    public FabricTabPlayer(ServerPlayer player) {
        super(player, player.getUUID(), player.getGameProfile().getName(), player.level().dimension().location().toString());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return FabricTAB.hasPermission(getPlayer().createCommandSourceStack(), permission);
    }

    @Override
    public int getPing() {
        return getPlayer().latency;
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
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
    public @Nullable TabList.Skin getSkin() {
        Collection<Property> properties = getPlayer().getGameProfile().getProperties().get(TabList.TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null; // Offline mode
        Property skinProperty = properties.iterator().next();
        return new TabList.Skin(skinProperty.getValue(), skinProperty.getSignature());
    }

    @Override
    public @NotNull ServerPlayer getPlayer() {
        return (ServerPlayer) player;
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
    public double getHealth() {
        return getPlayer().getHealth();
    }

    @Override
    public String getDisplayName() {
        return getPlayer().getDisplayName().getString(); // Will make it work properly if someone asks
    }

    /**
     * Sends packet to the player
     *
     * @param   packet
     *          Packet to send
     */
    public void sendPacket(Packet<?> packet) {
        getPlayer().connection.send(packet);
    }
}
