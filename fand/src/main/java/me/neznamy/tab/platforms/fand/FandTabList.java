package me.neznamy.tab.platforms.fand;

import io.fand.api.Fand;
import io.fand.api.entity.GameMode;
import io.fand.api.entity.Player;
import io.fand.api.player.PlayerProfile;
import io.fand.api.player.PlayerSkin;
import io.fand.api.plugin.PluginContext;
import io.fand.api.tablist.TabListEntry;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Per-viewer tab list implementation using Fand's structured packet API. */
public final class FandTabList extends TrackedTabList<FandTabPlayer> {

    private final PluginContext context;
    private final ConcurrentMap<UUID, TabListEntry> state = new ConcurrentHashMap<>();
    private final Set<UUID> managedEntries = ConcurrentHashMap.newKeySet();
    private final Set<UUID> removedEntries = ConcurrentHashMap.newKeySet();

    public FandTabList(@NotNull FandTabPlayer player, @NotNull PluginContext context) {
        super(player);
        this.context = context;
    }

    @Override
    public void removeEntry(@NotNull UUID entry) {
        state.remove(entry);
        managedEntries.remove(entry);
        removedEntries.add(entry);
        context.packets().sender().send(
                player.getPlayer(),
                context.packets().playerInfo().remove(List.of(entry)));
    }

    @Override
    public void updateDisplayName0(@NotNull UUID entry, @Nullable TabComponent displayName) {
        update(entry, value -> value.withDisplayName(displayName == null ? null : displayName.toAdventure()));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        update(entry, value -> value.withLatency(latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        update(entry, value -> value.withGameMode(gameMode(gameMode)));
    }

    @Override
    public void updateListed(@NotNull UUID entry, boolean listed) {
        update(entry, value -> value.withListed(listed));
    }

    @Override
    public void updateListOrder(@NotNull UUID entry, int listOrder) {
        update(entry, value -> value.withOrder(listOrder));
    }

    @Override
    public void updateHat(@NotNull UUID entry, boolean showHat) {
        update(entry, value -> value.withShowHat(showHat));
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
        entries.addAll(managedEntries);
        entries.removeAll(removedEntries);
        return List.copyOf(entries);
    }

    @Override
    @NotNull
    public Object onPacketSend(@NotNull Object packet) {
        return packet;
    }

    private void update(UUID entryId, UnaryOperator<TabListEntry> operation) {
        TabListEntry updated = state.compute(entryId, (ignored, current) ->
                operation.apply(current == null ? currentEntry(entryId) : current));
        context.packets().sender().send(
                player.getPlayer(),
                context.packets().playerInfo().update(List.of(updated)));
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
