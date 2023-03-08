package me.neznamy.tab.shared.backend;

import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import org.jetbrains.annotations.Nullable;

public abstract class BackendPlatform extends Platform {

    @Override
    public PermissionPlugin detectPermissionPlugin() {
        if (getPluginVersion(TabConstants.Plugin.LUCKPERMS) != null) {
            return new LuckPerms(getPluginVersion(TabConstants.Plugin.LUCKPERMS));
        }
        return new None();
    }

    @Override
    public @Nullable RedisSupport getRedisSupport(GlobalPlayerList global, PlayerList playerList, NameTag nameTags) {
        return null;
    }
}
