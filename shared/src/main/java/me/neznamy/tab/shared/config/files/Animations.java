package me.neznamy.tab.shared.config.files;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.converter.LegacyConverter;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.placeholders.animation.AnimationConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

@Getter
public class Animations {

    private final ConfigurationFile animationFile = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("config/animations.yml"),
            new File(TAB.getInstance().getDataFolder(), "animations.yml"));

    @NotNull private final AnimationConfiguration animations;

    public Animations() throws IOException {
        new LegacyConverter().convert2810to290(animationFile);
        animations = AnimationConfiguration.fromSection(animationFile.getConfigurationSection(""));
    }
}
