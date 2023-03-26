package me.neznamy.tab.platforms.krypton.features.unlimitedtags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.backend.EntityData;
import org.kryptonmc.krypton.entity.metadata.MetadataHolder;

@RequiredArgsConstructor
public class WrappedEntityData implements EntityData {

    private final MetadataHolder data;

    @Override
    public Object build() {
        return data;
    }
}
