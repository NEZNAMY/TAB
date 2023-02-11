package me.neznamy.tab.platforms.sponge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlay;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
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
import org.spongepowered.api.scoreboard.CollisionRule;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;

public final class SpongeTabPlayer extends ITabPlayer {

    private final Map<Class<? extends TabPacket>, Consumer<TabPacket>> packetMethods = new HashMap<>();
    {
        packetMethods.put(PacketPlayOutChat.class, packet -> handle((PacketPlayOutChat) packet));
        packetMethods.put(PacketPlayOutPlayerListHeaderFooter.class, packet -> handle((PacketPlayOutPlayerListHeaderFooter) packet));
        packetMethods.put(PacketPlayOutPlayerInfo.class, packet -> handle((PacketPlayOutPlayerInfo) packet));
        packetMethods.put(PacketPlayOutScoreboardDisplayObjective.class, packet -> handle((PacketPlayOutScoreboardDisplayObjective) packet));
        packetMethods.put(PacketPlayOutScoreboardObjective.class, packet -> handle((PacketPlayOutScoreboardObjective) packet));
        packetMethods.put(PacketPlayOutScoreboardScore.class, packet -> handle((PacketPlayOutScoreboardScore) packet));
        packetMethods.put(PacketPlayOutScoreboardTeam.class, packet -> handle((PacketPlayOutScoreboardTeam) packet));
        packetMethods.put(PacketPlayOutBoss.class, packet -> handle((PacketPlayOutBoss) packet));
    }

    private final Map<UUID, ServerBossBar> bossBars = new HashMap<>();
    private final Map<String, Objective> objectives = new HashMap<>();

    public SpongeTabPlayer(final Player player) {
        super(player, player.getUniqueId(), player.getName(), TAB.getInstance().getConfiguration().getServerName(), player.getWorld().getName(), ProtocolVersion.V1_12_2.getNetworkId(), true);
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

    private void handle(final PacketPlayOutChat packet) {
        final Text message = ComponentUtils.fromComponent(packet.getMessage(), getVersion());
        final ChatType type;
        switch (packet.getType()) {
            case CHAT:
                type = ChatTypes.CHAT;
                break;
            case SYSTEM:
                type = ChatTypes.SYSTEM;
                break;
            case GAME_INFO:
                type = ChatTypes.ACTION_BAR;
                break;
            default:
                throw new IllegalArgumentException("Unknown chat type: " + packet.getType());
        }
        getPlayer().sendMessage(type, message);
    }

    private void handle(final PacketPlayOutPlayerListHeaderFooter packet) {
        getPlayer().getTabList().setHeaderAndFooter(
                ComponentUtils.fromComponent(packet.getHeader(), getVersion()),
                ComponentUtils.fromComponent(packet.getFooter(), getVersion())
        );
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
                                .displayName(ComponentUtils.fromComponent(data.getDisplayName(), getVersion()))
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
                        list.getEntry(data.getUniqueId()).ifPresent(e -> e.setDisplayName(ComponentUtils.fromComponent(data.getDisplayName(), getVersion())));
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

    private void handle(final PacketPlayOutBoss packet) {
        ServerBossBar bar;
        switch (packet.getAction()) {
            case ADD:
                if (bossBars.containsKey(packet.getId())) return;
                bar = ServerBossBar.builder()
                        .name(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()))
                        .color(converBossBarColor(packet.getColor()))
                        .overlay(convertOverlay(packet.getOverlay()))
                        .percent(packet.getPct())
                        .createFog(packet.isCreateWorldFog())
                        .darkenSky(packet.isDarkenScreen())
                        .playEndBossMusic(packet.isPlayMusic())
                        .build();
                bossBars.put(packet.getId(), bar);
                bar.addPlayer(getPlayer());
                break;
            case REMOVE:
                bossBars.get(packet.getId()).removePlayer(getPlayer());
                bossBars.remove(packet.getId());
                break;
            case UPDATE_PCT:
                bossBars.get(packet.getId()).setPercent(packet.getPct());
                break;
            case UPDATE_NAME:
                bossBars.get(packet.getId()).setName(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()));
                break;
            case UPDATE_STYLE:
                bar = bossBars.get(packet.getId());
                bar.setColor(converBossBarColor(packet.getColor()));
                bar.setOverlay(convertOverlay(packet.getOverlay()));
                break;
            case UPDATE_PROPERTIES:
                bar = bossBars.get(packet.getId());
                bar.setCreateFog(packet.isCreateWorldFog());
                bar.setDarkenSky(packet.isDarkenScreen());
                bar.setPlayEndBossMusic(packet.isPlayMusic());
                break;
        }
    }

    private static BossBarColor converBossBarColor(final BarColor color) {
        switch (color) {
            case PINK:
                return BossBarColors.PINK;
            case BLUE:
                return BossBarColors.BLUE;
            case RED:
                return BossBarColors.RED;
            case GREEN:
                return BossBarColors.GREEN;
            case YELLOW:
                return BossBarColors.YELLOW;
            case PURPLE:
                return BossBarColors.PURPLE;
            case WHITE:
                return BossBarColors.WHITE;
            default:
                throw new IllegalArgumentException("Unknown boss bar color: " + color);
        }
    }

    private static BossBarOverlay convertOverlay(final BarStyle style) {
        switch (style) {
            case PROGRESS:
                return BossBarOverlays.PROGRESS;
            case NOTCHED_6:
                return BossBarOverlays.NOTCHED_6;
            case NOTCHED_10:
                return BossBarOverlays.NOTCHED_10;
            case NOTCHED_12:
                return BossBarOverlays.NOTCHED_12;
            case NOTCHED_20:
                return BossBarOverlays.NOTCHED_20;
            default:
                throw new IllegalArgumentException("Unknown boss bar overlay: " + style);
        }
    }

    private void handle(final PacketPlayOutScoreboardDisplayObjective packet) {
        final Objective objective = objectives.get(packet.getObjectiveName());
        final DisplaySlot slot = convertDisplaySlot(packet.getSlot());
        getPlayer().getScoreboard().updateDisplaySlot(objective, slot);
    }

    private static DisplaySlot convertDisplaySlot(final int slot) {
        switch (slot) {
            case 0:
                return DisplaySlots.LIST;
            case 1:
                return DisplaySlots.SIDEBAR;
            case 2:
                return DisplaySlots.BELOW_NAME;
            default:
                throw new IllegalArgumentException("Unknown display slot: " + slot);
        }
    }

    private void handle(final PacketPlayOutScoreboardObjective packet) {
        final Scoreboard scoreboard = getPlayer().getScoreboard();
        String displayName;
        switch (packet.getAction()) {
            case 0:
                displayName = cutToLength(packet.getDisplayName(), 32);
                final Objective objective = Objective.builder()
                        .name(packet.getObjectiveName())
                        .displayName(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(displayName), getVersion()))
                        .objectiveDisplayMode(convertDisplayMode(packet.getRenderType()))
                        .criterion(Criteria.DUMMY)
                        .build();
                objectives.put(packet.getObjectiveName(), objective);
                scoreboard.addObjective(objective);
            case 1:
                scoreboard.removeObjective(objectives.get(packet.getObjectiveName()));
            case 2:
                displayName = cutToLength(packet.getDisplayName(), 32);
                final Objective obj = objectives.get(packet.getObjectiveName());
                obj.setDisplayName(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(displayName), getVersion()));
                if (packet.getRenderType() != null) obj.setDisplayMode(convertDisplayMode(packet.getRenderType()));
        }
    }

    private static ObjectiveDisplayMode convertDisplayMode(final PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay mode) {
        switch (mode) {
            case INTEGER:
                return ObjectiveDisplayModes.INTEGER;
            case HEARTS:
                return ObjectiveDisplayModes.HEARTS;
            default:
                throw new IllegalArgumentException("Unknown display mode: " + mode);
        }
    }

    private void handle(final PacketPlayOutScoreboardScore packet) {
        switch (packet.getAction()) {
            case CHANGE:
                final Objective objective = objectives.get(packet.getObjectiveName());
                final Text scoreName = ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(packet.getPlayer()), getVersion());
                final Score score = objective.getOrCreateScore(scoreName);
                score.setScore(packet.getScore());
            case REMOVE:
                final Objective objective1 = objectives.get(packet.getObjectiveName());
                final Text name = ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(packet.getPlayer()), getVersion());
                objective1.removeScore(name);
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
                        .displayName(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()))
                        .prefix(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(prefix), getVersion()))
                        .suffix(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(suffix), getVersion()))
                        .allowFriendlyFire((packet.getOptions() & 0x01) != 0)
                        .canSeeFriendlyInvisibles((packet.getOptions() & 0x02) != 0)
                        .collisionRule(convertCollisionRule(packet.getCollisionRule()))
                        .nameTagVisibility(convertVisibility(packet.getNameTagVisibility()))
                        .build();
                scoreboard.registerTeam(team);
            case 1:
                scoreboard.getTeam(packet.getName()).ifPresent(Team::unregister);
            case 2:
                team = scoreboard.getTeam(packet.getName()).orElse(null);
                if (team == null) return;

                prefix = cutToLength(packet.getPlayerPrefix(), 16);
                suffix = cutToLength(packet.getPlayerSuffix(), 16);
                team.setDisplayName(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(packet.getName()), getVersion()));
                team.setPrefix(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(prefix), getVersion()));
                team.setSuffix(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(suffix), getVersion()));
                team.setAllowFriendlyFire((packet.getOptions() & 0x01) != 0);
                team.setCanSeeFriendlyInvisibles((packet.getOptions() & 0x02) != 0);
                team.setCollisionRule(convertCollisionRule(packet.getCollisionRule()));
                team.setNameTagVisibility(convertVisibility(packet.getNameTagVisibility()));
            case 3:
                team = scoreboard.getTeam(packet.getName()).orElse(null);
                if (team == null) return;

                for (final String member : packet.getPlayers()) {
                    team.addMember(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(member), getVersion()));
                }
            case 4:
                team = scoreboard.getTeam(packet.getName()).orElse(null);
                if (team == null) return;

                for (final String member : packet.getPlayers()) {
                    team.removeMember(ComponentUtils.fromComponent(IChatBaseComponent.optimizedComponent(member), getVersion()));
                }
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
}
