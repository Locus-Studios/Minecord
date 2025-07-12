package me.locusdevs.minecord;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private final File configFile;
    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode root;

    public ConfigManager(Path dataDirectory) throws IOException {
        this.configFile = dataDirectory.resolve("config.yml").toFile();

        if (!configFile.exists()) {
            dataDirectory.toFile().mkdirs();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                } else {
                    throw new IOException("Default file 'config.yml' not found in jar.");
                }
            }
        }

        this.loader = YamlConfigurationLoader.builder()
                .file(configFile)
                .build();

        load();
    }

    public void load() throws IOException {
        this.root = loader.load();
    }

    public void save() throws IOException {
        loader.save(root);
    }

    public CommentedConfigurationNode get() {
        return root;
    }

    public String getString(String path, String def) {
        String[] keys = path.split("\\.");
        CommentedConfigurationNode node = root;
        for (String key : keys) {
            node = node.node(key);
        }
        return node.getString(def);
    }

    public int getInt(String path, int def) {
        String[] keys = path.split("\\.");
        CommentedConfigurationNode node = root;
        for (String key : keys) {
            node = node.node(key);
        }
        return node.getInt(def);
    }
}
