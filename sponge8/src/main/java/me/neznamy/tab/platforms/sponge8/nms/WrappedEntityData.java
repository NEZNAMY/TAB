package me.neznamy.tab.platforms.sponge8.nms;

import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.backend.EntityData;
import net.minecraft.network.syncher.SynchedEntityData;

@AllArgsConstructor
public class WrappedEntityData implements EntityData {

    private final SynchedEntityData data;

    @Override
    public Object build() {
        return data;
    }
}
