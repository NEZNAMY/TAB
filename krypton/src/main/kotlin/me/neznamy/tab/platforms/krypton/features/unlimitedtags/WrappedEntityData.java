package me.neznamy.tab.platforms.krypton.features.unlimitedtags;

import me.neznamy.tab.shared.backend.EntityData;
import org.kryptonmc.krypton.entity.metadata.MetadataHolder;

public class WrappedEntityData implements EntityData {

    private final MetadataHolder data;

    public WrappedEntityData(MetadataHolder data) {
        this.data = data;
    }

    @Override
    public Object build() {
        return data;
    }
}
