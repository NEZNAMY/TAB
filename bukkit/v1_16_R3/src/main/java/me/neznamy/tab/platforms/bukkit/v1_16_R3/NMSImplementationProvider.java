package me.neznamy.tab.platforms.bukkit.v1_16_R3;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaScoreboard;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaTabList;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation provider using direct NMS code for 1.16.5.
 */
@Getter
public class NMSImplementationProvider implements ImplementationProvider {

    @NotNull
    private final ComponentConverter componentConverter = new NMSComponentConverter();
    
    @Override
    @NotNull
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
        return new NMSPacketScoreboard(player);
    }

    @Override
    public void onPacketSend(@NonNull Object packet, @NonNull ViaScoreboard scoreboard) {
        NMSPacketScoreboard.onPacketSend(packet, scoreboard);
    }

    @Override
    public void onPacketSend(@NonNull Object packet, @NonNull ViaTabList tabList) {
        NMSPacketTabList.onPacketSend(packet, tabList);
    }

    @Override
    @NotNull
    public TabList newTabList(@NotNull BukkitTabPlayer player) {
        return new NMSPacketTabList(player);
    }

    @Override
    @Nullable
    public TabList.Skin getSkin(@NotNull BukkitTabPlayer player) {
        return NMSPacketTabList.getSkin(player);
    }

    @Override
    @NotNull
    public FunctionWithException<BukkitTabPlayer, Channel> getChannelFunction() {
        return player -> ((CraftPlayer)player.getPlayer()).getHandle().playerConnection.networkManager.channel;
    }

    @Override
    public int getPing(@NotNull BukkitTabPlayer player) {
        return ((CraftPlayer)player.getPlayer()).getHandle().ping;
    }
}