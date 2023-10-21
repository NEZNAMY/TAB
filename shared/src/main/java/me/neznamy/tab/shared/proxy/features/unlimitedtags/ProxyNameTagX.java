package me.neznamy.tab.shared.proxy.features.unlimitedtags;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class ProxyNameTagX extends NameTagX {

    public ProxyNameTagX() {
        super(ProxyArmorStandManager::new);
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        super.onServerChange(p, from, to);
        if (isPreviewingNameTag(p)) {
            ((ProxyTabPlayer)p).sendPluginMessage("NameTagX", "Preview", true);
        }
        for (String line : getDefinedLines()) {
            String text = p.getProperty(line).get();
            ((ProxyTabPlayer)p).sendPluginMessage("NameTagX", "SetText", line, text, IChatBaseComponent.fromColoredText(text).toString(p.getVersion())); //rel placeholder support in the future
        }
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        super.onWorldChange(changed, from, to);
        for (String line : getDefinedLines()) {
            String text = changed.getProperty(line).get();
            ((ProxyTabPlayer)changed).sendPluginMessage("NameTagX", "SetText", line, text, IChatBaseComponent.fromColoredText(text).toString(changed.getVersion())); //rel placeholder support in the future
        }
    }

    @Override
    public void onUnlimitedDisableConditionChange(TabPlayer p, boolean disabledNow) {
        super.onUnlimitedDisableConditionChange(p, disabledNow);
        ((ProxyTabPlayer)p).sendPluginMessage("NameTagX", "SetEnabled", !disabledNow && !getDisableChecker().isDisabledPlayer(p));
    }

    @Override
    public void addDisabledPlayer(@NotNull TabPlayer player) {
        super.addDisabledPlayer(player);
        ((ProxyTabPlayer)player).sendPluginMessage("NameTagX", "SetEnabled", false);
    }

    @Override
    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        super.onDisableConditionChange(p, disabledNow);
        ((ProxyTabPlayer)p).sendPluginMessage("NameTagX", "SetEnabled", !disabledNow && !getUnlimitedDisableChecker().isDisabledPlayer(p));
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
        ((ProxyTabPlayer)player).sendPluginMessage("NameTagX", "Preview", status);
    }

    @Override
    public void resumeArmorStands(@NotNull TabPlayer player) {
        ((ProxyTabPlayer)player).sendPluginMessage("NameTagX", "Resume");
    }

    @Override
    public void pauseArmorStands(@NotNull TabPlayer player) {
        ((ProxyTabPlayer)player).sendPluginMessage("NameTagX", "Pause");
    }

    @Override
    public void updateNameTagVisibilityView(@NotNull TabPlayer player) {
        ((ProxyTabPlayer)player).sendPluginMessage("NameTagX", "VisibilityView");
    }
}
