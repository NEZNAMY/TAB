package me.neznamy.tab.platforms.sponge7;

import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
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
import org.spongepowered.api.scoreboard.*;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
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
        packetMethods.put(PacketPlayOutScoreboardTeam.class, packet -> handle((PacketPlayOutScoreboardTeam) packet));
    }

    private final Map<UUID, ServerBossBar> bossBars = new HashMap<>();
    private final Map<String, Objective> objectives = new HashMap<>();

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

    private void handle(final PacketPlayOutScoreboardTeam packet) {
        final Scoreboard scoreboard = getPlayer().getScoreboard();
        String prefix;
        String suffix;
        Team team;
        switch (packet.getAction()) {
            case 0:
                prefix = cutToLength(packet.getPlayerPrefix(), 16);
                suffix = cutToLength(packet.getPlayerSuffix(), 16);
                team = Team.builder()
                        .name(packet.getName())
                        .displayName(textCache.get(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()))
                        .prefix(textCache.get(IChatBaseComponent.optimizedComponent(prefix), getVersion()))
                        .suffix(textCache.get(IChatBaseComponent.optimizedComponent(suffix), getVersion()))
                        .allowFriendlyFire((packet.getOptions() & 0x01) != 0)
                        .canSeeFriendlyInvisibles((packet.getOptions() & 0x02) != 0)
                        .collisionRule(convertCollisionRule(packet.getCollisionRule()))
                        .nameTagVisibility(convertVisibility(packet.getNameTagVisibility()))
                        .build();
                for (final String member : packet.getPlayers()) {
                    team.addMember(textCache.get(IChatBaseComponent.optimizedComponent(member), getVersion()));
                }
                scoreboard.registerTeam(team);
                break;
            case 1:
                scoreboard.getTeam(packet.getName()).ifPresent(Team::unregister);
                break;
            case 2:
                team = scoreboard.getTeam(packet.getName()).orElse(null);
                if (team == null) return;

                prefix = cutToLength(packet.getPlayerPrefix(), 16);
                suffix = cutToLength(packet.getPlayerSuffix(), 16);
                team.setDisplayName(textCache.get(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()));
                team.setPrefix(textCache.get(IChatBaseComponent.optimizedComponent(prefix), getVersion()));
                team.setSuffix(textCache.get(IChatBaseComponent.optimizedComponent(suffix), getVersion()));
                team.setAllowFriendlyFire((packet.getOptions() & 0x01) != 0);
                team.setCanSeeFriendlyInvisibles((packet.getOptions() & 0x02) != 0);
                team.setCollisionRule(convertCollisionRule(packet.getCollisionRule()));
                team.setNameTagVisibility(convertVisibility(packet.getNameTagVisibility()));
                break;
            case 3:
                team = scoreboard.getTeam(packet.getName()).orElse(null);
                if (team == null) return;

                for (final String member : packet.getPlayers()) {
                    team.addMember(textCache.get(IChatBaseComponent.optimizedComponent(member), getVersion()));
                }
                break;
            case 4:
                team = scoreboard.getTeam(packet.getName()).orElse(null);
                if (team == null) return;

                for (final String member : packet.getPlayers()) {
                    team.removeMember(textCache.get(IChatBaseComponent.optimizedComponent(member), getVersion()));
                }
                break;
        }
    }

    private static String cutToLength(String text, final int maxLength) {
        if (text.length() > maxLength) text = text.substring(0, maxLength);
        return text;
    }

    private static CollisionRule convertCollisionRule(final String rule) {
        switch (rule) {
            case "always":
                return CollisionRules.ALWAYS;
            case "never":
                return CollisionRules.NEVER;
            case "pushOtherTeams":
                return CollisionRules.PUSH_OTHER_TEAMS;
            case "pushOwnTeam":
                return CollisionRules.PUSH_OWN_TEAM;
            default:
                throw new IllegalArgumentException("Unknown collision rule: " + rule);
        }
    }

    private static Visibility convertVisibility(final String visibility) {
        switch (visibility) {
            case "always":
                return Visibilities.ALWAYS;
            case "never":
                return Visibilities.NEVER;
            case "hideForOtherTeams":
                return Visibilities.HIDE_FOR_OTHER_TEAMS;
            case "hideForOwnTeam":
                return Visibilities.HIDE_FOR_OWN_TEAM;
            default:
                throw new IllegalArgumentException("Unknown visibility: " + visibility);
        }
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

    @Override
    public void setObjectiveDisplaySlot(int slot, @NonNull String objective) {
        getPlayer().getScoreboard().updateDisplaySlot(objectives.get(objective), convertDisplaySlot(slot));
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        String displayName = cutToLength(title, 32);
        Objective objective = Objective.builder()
                .name(objectiveName)
                .displayName(textCache.get(IChatBaseComponent.optimizedComponent(displayName), getVersion()))
                .objectiveDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER)
                .criterion(Criteria.DUMMY)
                .build();
        objectives.put(objectiveName, objective);
        getPlayer().getScoreboard().addObjective(objective);
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        getPlayer().getScoreboard().removeObjective(objectives.get(objectiveName));
    }

    @Override
    public void updateObjectiveTitle0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        String displayName = cutToLength(title, 32);
        Objective obj = objectives.get(objectiveName);
        obj.setDisplayName(textCache.get(IChatBaseComponent.optimizedComponent(displayName), getVersion()));
        obj.setDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER);
    }

    @Override
    public void setScoreboardScore0(@NonNull String objective, @NonNull String player, int score) {
        objectives.get(objective).getOrCreateScore(textCache.get(IChatBaseComponent.optimizedComponent(player), getVersion())).setScore(score);
    }

    @Override
    public void removeScoreboardScore0(@NonNull String objective, @NonNull String player) {
        objectives.get(objective).removeScore(textCache.get(IChatBaseComponent.optimizedComponent(player), getVersion()));
    }

    private static DisplaySlot convertDisplaySlot(final int slot) {
        switch (slot) {
            case 0: return DisplaySlots.LIST;
            case 1: return DisplaySlots.SIDEBAR;
            default: return DisplaySlots.BELOW_NAME;
        }
    }
}
