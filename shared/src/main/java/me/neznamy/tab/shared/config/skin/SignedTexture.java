package me.neznamy.tab.shared.config.skin;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabList.Skin;
import org.jetbrains.annotations.NotNull;

/**
 * Skin source using raw texture;signature.
 */
public class SignedTexture extends SkinSource {

    protected SignedTexture(@NotNull ConfigurationFile file) {
        super(file, "signed_textures");
    }

    @Override
    @NotNull
    public Skin download(@NotNull String textureBase64) {
        String[] parts = textureBase64.split(";");
        String base64 = parts[0];
        String signature = parts.length > 1 ? parts[1] : "";
        return new Skin(base64, signature);
    }
}
