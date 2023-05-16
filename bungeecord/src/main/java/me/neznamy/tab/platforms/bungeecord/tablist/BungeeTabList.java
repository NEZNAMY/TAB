package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ComponentCache;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class BungeeTabList implements TabList {

    /** Component cache for BungeeCord components */
    private static final @NotNull ComponentCache<IChatBaseComponent, BaseComponent> bungeeCache =
            new ComponentCache<>(10000, BungeeTabList::toBungeeComponent);

    /** Player this TabList belongs to */
    protected final BungeeTabPlayer player;

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().setTabHeader(
                bungeeCache.get(header, player.getVersion()),
                bungeeCache.get(footer, player.getVersion())
        );
    }

    public @NotNull Item item(@NotNull UUID id) {
        Item item = new Item();
        item.setUuid(id);
        return item;
    }

    /**
     * Converts this component to bungeecord component.
     *
     * @return  BungeeCord component from this component.
     */
    private static TextComponent toBungeeComponent(@NotNull IChatBaseComponent component, ProtocolVersion clientVersion) {
        TextComponent textComponent = new TextComponent(component.getText());
        if (component.getModifier().getColor() != null) textComponent.setColor(ChatColor.of(
                component.getModifier().getColor().toString(clientVersion.getMinorVersion() >= 16)));
        if (component.getModifier().isBold()) textComponent.setBold(true);
        if (component.getModifier().isItalic()) textComponent.setItalic(true);
        if (component.getModifier().isObfuscated()) textComponent.setObfuscated(true);
        if (component.getModifier().isStrikethrough()) textComponent.setStrikethrough(true);
        if (component.getModifier().isUnderlined()) textComponent.setUnderlined(true);
        if (!component.getExtra().isEmpty()) textComponent.setExtra(
                component.getExtra().stream().map(c -> toBungeeComponent(c, clientVersion)).collect(Collectors.toList()));
        return textComponent;
    }
}
