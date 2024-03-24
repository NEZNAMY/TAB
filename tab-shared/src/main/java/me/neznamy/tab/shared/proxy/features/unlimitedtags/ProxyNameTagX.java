package me.neznamy.tab.shared.proxy.features.unlimitedtags;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.nametags.*;
import org.jetbrains.annotations.NotNull;

/**
 * Unlimited nametag mode implementation for proxies.
 */
public class ProxyNameTagX extends NameTagX {

    /**
     * Constructs new instance.
     */
    public ProxyNameTagX() {
        super(ProxyArmorStandManager::new);
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        super.onServerChange(p, from, to);
        if (isPreviewingNameTag(p)) {
            ((ProxyTabPlayer)p).sendPluginMessage(new Preview(true));
        }
        for (String line : getDefinedLines()) {
            String text = p.getProperty(line).get();
            ((ProxyTabPlayer)p).sendPluginMessage(new SetText(line, text));
        }
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        super.onWorldChange(changed, from, to);
        for (String line : getDefinedLines()) {
            String text = changed.getProperty(line).get();
            ((ProxyTabPlayer)changed).sendPluginMessage(new SetText(line, text));
        }
    }

    @Override
    public void onUnlimitedDisableConditionChange(TabPlayer p, boolean disabledNow) {
        super.onUnlimitedDisableConditionChange(p, disabledNow);
        ((ProxyTabPlayer)p).sendPluginMessage(new SetEnabled(!disabledNow && !getDisableChecker().isDisabledPlayer(p)));
    }

    @Override
    public void addDisabledPlayer(@NotNull TabPlayer player) {
        super.addDisabledPlayer(player);
        ((ProxyTabPlayer)player).sendPluginMessage(new SetEnabled(false));
    }

    @Override
    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        super.onDisableConditionChange(p, disabledNow);
        ((ProxyTabPlayer)p).sendPluginMessage(new SetEnabled(!disabledNow && !getUnlimitedDisableChecker().isDisabledPlayer(p)));
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        super.onQuit(disconnectedPlayer);
        armorStandManagerMap.remove(disconnectedPlayer); // WeakHashMap doesn't clear this due to value referencing the key
    }

    @Override
    public boolean isOnBoat(@NotNull TabPlayer player) {
        return ((ProxyTabPlayer)player).isOnBoat();
    }

    @Override
    public void setNameTagPreview(@NotNull TabPlayer player, boolean status) {
        ((ProxyTabPlayer)player).sendPluginMessage(new Preview(status));
    }

    @Override
    public void resumeArmorStands(@NotNull TabPlayer player) {
        ((ProxyTabPlayer)player).sendPluginMessage(new Resume());
    }

    @Override
    public void pauseArmorStands(@NotNull TabPlayer player) {
        ((ProxyTabPlayer)player).sendPluginMessage(new Pause());
    }

    @Override
    public void updateNameTagVisibilityView(@NotNull TabPlayer player) {
        ((ProxyTabPlayer)player).sendPluginMessage(new VisibilityView());
    }
}
