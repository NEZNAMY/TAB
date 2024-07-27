
package me.neznamy.tab.shared.features.layout.skin;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import me.neznamy.tab.shared.config.file.ConfigurationFile;

/**
 * Skin source using raw texture;signature.
 */
public class SignedTexture extends SkinSource {

    protected SignedTexture(@NotNull ConfigurationFile file) {
        super(file, "signed_textures");
    }

    @Override
    @NotNull
    public List<String> download(@NotNull String textureBase64) {
        String[] parts = textureBase64.split(";");
        String base64 = parts[0];
        String signature = parts.length > 1 ? parts[1] : "";
        return Arrays.asList(base64, signature);
    }
}
