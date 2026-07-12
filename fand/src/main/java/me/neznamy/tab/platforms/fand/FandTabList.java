package me.neznamy.tab.platforms.fand;

import io.fand.api.Fand;
import io.fand.api.entity.GameMode;
import io.fand.api.entity.Player;
import io.fand.api.packet.PlayerInfoEntry;
import io.fand.api.packet.view.ClientboundPlayerInfoRemovePacketView;
import io.fand.api.packet.view.ClientboundPlayerInfoUpdatePacketView;
import io.fand.api.packet.view.ClientboundTabListPacketView;
import io.fand.api.player.PlayerProfile;
import io.fand.api.player.PlayerSkin;
import io.fand.api.plugin.PluginContext;
import io.fand.api.tablist.TabListEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Per-viewer tab list implementation using Fand's structured packet API. */
public final class FandTabList extends TrackedTabList<FandTabPlayer> {

    private final PluginContext context;
    private final ConcurrentMap<UUID, TabListEntry> state = new ConcurrentHashMap<>();
    private final Set<UUID> managedEntries = ConcurrentHashMap.newKeySet();
    private final Set<UUID> observedEntries = ConcurrentHashMap.newKeySet();
    private final Set<UUID> removedEntries = ConcurrentHashMap.newKeySet();

    public FandTabList(@NotNull FandTabPlayer player, @NotNull PluginContext context) {
        super(player);
        this.context = context;
    }

    @Override
    public void removeEntry(@NotNull UUID entry) {
        state.remove(entry);
        managedEntries.remove(entry);
        observedEntries.remove(entry);
        removedEntries.add(entry);
        context.packets().sender().send(
                player.getPlayer(),
                context.packets().playerInfo().remove(List.of(entry)));
    }

    @Override
    public void updateDisplayName0(@NotNull UUID entry, @Nullable TabComponent displayName) {
        update(
                entry,
                value -> value.withDisplayName(displayName == null ? null : displayName.toAdventure()),
                "UPDATE_DISPLAY_NAME");
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        update(entry, value -> value.withLatency(latency), "UPDATE_LATENCY");
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        update(entry, value -> value.withGameMode(gameMode(gameMode)), "UPDATE_GAME_MODE");
    }

    @Override
    public void updateListed(@NotNull UUID entry, boolean listed) {
        update(entry, value -> value.withListed(listed), "UPDATE_LISTED");
    }

    @Override
    public void updateListOrder(@NotNull UUID entry, int listOrder) {
        update(entry, value -> value.withOrder(listOrder), "UPDATE_LIST_ORDER");
    }

    @Override
    public void updateHat(@NotNull UUID entry, boolean showHat) {
        update(entry, value -> value.withShowHat(showHat), "UPDATE_HAT");
    }

    @Override
    public void addEntry0(@NotNull Entry entry) {
        PlayerSkin skin = entry.getSkin() == null
                ? null
                : new PlayerSkin(entry.getSkin().getValue(), entry.getSkin().getSignature());
        PlayerProfile profile = new PlayerProfile(entry.getUniqueId(), entry.getName(), skin);
        TabListEntry fandEntry = TabListEntry.builder(profile)
                .listed(entry.isListed())
                .latency(entry.getLatency())
                .gameMode(gameMode(entry.getGameMode()))
                .displayName(entry.getDisplayName() == null ? null : entry.getDisplayName().toAdventure())
                .order(entry.getListOrder())
                .showHat(entry.isShowHat())
                .build();
        state.put(entry.getUniqueId(), fandEntry);
        managedEntries.add(entry.getUniqueId());
        observedEntries.add(entry.getUniqueId());
        removedEntries.remove(entry.getUniqueId());
        context.packets().sender().send(
                player.getPlayer(),
                context.packets().playerInfo().add(List.of(fandEntry)));
    }

    @Override
    public void setPlayerListHeaderFooter0(@NotNull TabComponent header, @NotNull TabComponent footer) {
        player.getPlayer().sendTabList(header.toAdventure(), footer.toAdventure());
    }

    @Override
    @Nullable
    public Skin getSkin() {
        return player.getPlayer().skin()
                .map(skin -> new Skin(skin.value(), skin.signatureOrNull()))
                .orElse(null);
    }

    @Override
    public boolean containsEntry(@NotNull UUID entry) {
        return getEntries().contains(entry);
    }

    @Override
    @NotNull
    public Collection<UUID> getEntries() {
        Set<UUID> entries = new LinkedHashSet<>();
        Player viewer = player.getPlayer();
        for (Player target : Fand.server().players()) {
            UUID targetId = target.uniqueId();
            if (!removedEntries.contains(targetId) && context.tabLists().visible(viewer, target)) {
                entries.add(targetId);
            }
        }
        entries.addAll(observedEntries);
        entries.addAll(managedEntries);
        entries.removeAll(removedEntries);
        return List.copyOf(entries);
    }

    @Override
    @NotNull
    public Object onPacketSend(@NotNull Object packet) {
        if (packet instanceof ClientboundPlayerInfoUpdatePacketView playerInfo) {
            return rewritePlayerInfo(playerInfo);
        }
        return packet;
    }

    @NotNull
    ClientboundPlayerInfoUpdatePacketView rewritePlayerInfo(
            @NotNull ClientboundPlayerInfoUpdatePacketView packet
    ) {
        List<String> actions = packet.actions();
        List<PlayerInfoEntry> entries = new ArrayList<>(packet.entries().size());
        Integer forcedLatency = getForcedLatency();
        boolean rewrite = false;
        for (PlayerInfoEntry entry : packet.entries()) {
            PlayerInfoEntry updated = entry;
            UUID profileId = entry.profileId();
            if (actions.contains("UPDATE_DISPLAY_NAME")) {
                TabComponent forcedName = getForcedDisplayNames().get(profileId);
                if (forcedName != null && !Objects.equals(forcedName.toAdventure(), updated.displayName())) {
                    updated = updated.withDisplayName(forcedName.toAdventure());
                }
            }
            if (actions.contains("UPDATE_GAME_MODE")
                    && getBlockedSpectators().contains(profileId)
                    && updated.gameMode() == GameMode.SPECTATOR) {
                updated = updated.withGameMode(GameMode.SURVIVAL);
            }
            if (actions.contains("UPDATE_LATENCY") && forcedLatency != null
                    && updated.latency() != forcedLatency) {
                updated = updated.withLatency(forcedLatency);
            }
            if (actions.contains("UPDATE_LISTED") && allPlayersHidden
                    && profileId.getMostSignificantBits() != 0 && updated.listed()) {
                updated = updated.withListed(false);
            }
            if (actions.contains("ADD_PLAYER")) {
                observedEntries.add(profileId);
                removedEntries.remove(profileId);
                if (entry.profile() != null) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(
                            player, profileId, entry.profile().name());
                }
            }
            rewrite |= updated != entry;
            entries.add(updated);
        }
        return rewrite ? packet.withEntries(List.copyOf(entries)) : packet;
    }

    void observePlayerInfoRemove(@NotNull ClientboundPlayerInfoRemovePacketView packet) {
        for (Object value : packet.profileIds()) {
            if (value instanceof UUID profileId) {
                state.remove(profileId);
                managedEntries.remove(profileId);
                observedEntries.remove(profileId);
                removedEntries.add(profileId);
            }
        }
    }

    @NotNull
    ClientboundTabListPacketView rewriteHeaderFooter(@NotNull ClientboundTabListPacketView packet) {
        if (header == null || footer == null) {
            return packet;
        }
        Component expectedHeader = header.toAdventure();
        Component expectedFooter = footer.toAdventure();
        Component currentHeader = packet.value("header", Component.class);
        Component currentFooter = packet.value("footer", Component.class);
        if (Objects.equals(currentHeader, expectedHeader) && Objects.equals(currentFooter, expectedFooter)) {
            return packet;
        }
        return packet.with("header", expectedHeader)
                .with("footer", expectedFooter)
                .as(ClientboundTabListPacketView.class);
    }

    private void update(UUID entryId, UnaryOperator<TabListEntry> operation, String action) {
        TabListEntry updated = state.compute(entryId, (ignored, current) ->
                operation.apply(current == null ? currentEntry(entryId) : current));
        context.packets().sender().send(
                player.getPlayer(),
                context.packets().playerInfo().update(List.of(updated))
                        .with("actions", List.of(action)));
    }

    private static TabListEntry currentEntry(UUID entryId) {
        return Fand.server().player(entryId)
                .map(FandTabList::realPlayerEntry)
                .orElseGet(() -> TabListEntry.builder(entryId, entryId.toString().substring(0, 16)).build());
    }

    private static TabListEntry realPlayerEntry(Player player) {
        return TabListEntry.builder(player.profile())
                .listed(true)
                .latency(player.ping())
                .gameMode(player.gameMode())
                .displayName(player.tabListDisplayName().orElse(null))
                .order(player.tabListOrder())
                .showHat(true)
                .build();
    }

    private static GameMode gameMode(int gameMode) {
        GameMode[] values = GameMode.values();
        return gameMode >= 0 && gameMode < values.length ? values[gameMode] : GameMode.SURVIVAL;
    }
}
