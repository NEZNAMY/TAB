package me.neznamy.tab.shared.config.files.animations;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.Converter;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

@Getter
public class Animations {

    private final ConfigurationFile animationFile = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("config/animations.yml"),
            new File(TAB.getInstance().getDataFolder(), "animations.yml"));

    @NotNull private final AnimationConfiguration animations;

    public Animations() throws IOException {
        new Converter().convert2810to290(animationFile);
        animations = new AnimationConfiguration(animationFile);
    }
}
