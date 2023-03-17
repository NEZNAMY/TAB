package me.neznamy.tab.platforms.sponge7;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import org.spongepowered.api.boss.*;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class SpongeTabPlayer extends ITabPlayer {

    private static final ComponentCache<IChatBaseComponent, Text> textCache = new ComponentCache<>(10000,
            (component, version) -> TextSerializers.JSON.deserialize(component.toString(version)));

    private final Map<Class<? extends TabPacket>, Consumer<TabPacket>> packetMethods = new HashMap<>();
    {
        packetMethods.put(PacketPlayOutPlayerInfo.class, packet -> handle((PacketPlayOutPlayerInfo) packet));
    }

    private final Map<UUID, ServerBossBar> bossBars = new HashMap<>();

    @Getter private final Scoreboard scoreboard = new SpongeScoreboard(this);

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
        if (packet == null) return;
        packetMethods.get(packet.getClass()).accept((TabPacket) packet);
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendMessage(textCache.get(message, getVersion()));
    }

    private void handle(final PacketPlayOutPlayerInfo packet) {
        final TabList list = getPlayer().getTabList();
        for (final PlayerInfoData data : packet.getEntries()) {
            for (final EnumPlayerInfoAction action : packet.getActions()) {
                switch (action) {
                    case ADD_PLAYER:
                        if (list.getEntry(data.getUniqueId()).isPresent()) continue;

                        final GameProfile profile = GameProfile.of(data.getUniqueId(), data.getName());
                        if (data.getSkin() != null) {
                            profile.addProperty(ProfileProperty.of("textures", data.getSkin().getValue(), data.getSkin().getSignature()));
                        }

                        final TabListEntry entry = TabListEntry.builder()
                                .list(list)
                                .displayName(textCache.get(data.getDisplayName(), getVersion()))
                                .gameMode(convertGameMode(data.getGameMode()))
                                .profile(profile)
                                .latency(data.getLatency())
                                .build();
                        list.addEntry(entry);
                        break;
                    case REMOVE_PLAYER:
                        list.removeEntry(data.getUniqueId());
                        break;
                    case UPDATE_DISPLAY_NAME:
                        list.getEntry(data.getUniqueId()).ifPresent(e -> e.setDisplayName(textCache.get(data.getDisplayName(), getVersion())));
                        break;
                    case UPDATE_LATENCY:
                        list.getEntry(data.getUniqueId()).ifPresent(e -> e.setLatency(data.getLatency()));
                        break;
                    case UPDATE_GAME_MODE:
                        list.getEntry(data.getUniqueId()).ifPresent(e -> e.setGameMode(convertGameMode(data.getGameMode())));
                        break;
                    // Neither of these are supported in 1.12
                    case UPDATE_LISTED:
                    case INITIALIZE_CHAT:
                    default:
                        break;
                }
            }
        }
    }

    private static GameMode convertGameMode(final PacketPlayOutPlayerInfo.EnumGamemode mode) {
        switch (mode) {
            case NOT_SET:
                return GameModes.NOT_SET;
            case SURVIVAL:
                return GameModes.SURVIVAL;
            case CREATIVE:
                return GameModes.CREATIVE;
            case ADVENTURE:
                return GameModes.ADVENTURE;
            case SPECTATOR:
                return GameModes.SPECTATOR;
            default:
                throw new IllegalArgumentException("Unknown gamemode: " + mode);
        }
    }

    private static String cutToLength(String text, final int maxLength) {
        if (text.length() > maxLength) text = text.substring(0, maxLength);
        return text;
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
        getPlayer().getTabList().setHeaderAndFooter(textCache.get(header, version), textCache.get(footer, version));
    }

    @Override
    public void sendBossBar(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        ServerBossBar bar = ServerBossBar.builder()
                .name(textCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()))
                .color(convertBossBarColor(color))
                .overlay(convertOverlay(style))
                .percent(progress)
                .build();
        bossBars.put(id, bar);
        bar.addPlayer(getPlayer());
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull String title) {
        bossBars.get(id).setName(textCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()));
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
