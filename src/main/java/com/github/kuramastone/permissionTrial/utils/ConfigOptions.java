package com.github.kuramastone.permissionTrial.utils;

import com.github.kuramastone.permissionTrial.PermissionTrial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigOptions {

    @YamlConfig.YamlKey("Database.url")
    public String databaseURL;
    @YamlConfig.YamlKey("Database.name")
    public String databaseName;
    @YamlConfig.YamlKey("Database.user")
    public String databaseUser;
    @YamlConfig.YamlKey("Database.password")
    public String databasePassword;
    @YamlConfig.YamlKey("Database.useLocalTestingDatabase")
    public boolean databaseUseH2;

    public Map<String, ComponentEditor> messages;

    public void load() {
        YamlConfig config = new YamlConfig(YamlConfig.defaultDataFolder(), "config.yml");
        YamlConfig.loadFromYaml(this, config);
        loadMessages(config);
    }

    /**
     * Loads all keys under "messages" and stores them for easy retrieval elsewhere
     */
    public void loadMessages(YamlConfig config) {
        messages = new HashMap<>();
        config.installNewKeysFromDefault("messages", true);

        for (String subkey : config.getKeys("messages", true)) {
            String key = "messages." + subkey;
            if (config.isSection(key)) {
                continue;
            }

            String string;
            Object obj = config.getObject(key);
            if(obj instanceof List<?> list) {
                string = String.join("\n", list.toArray(new String[0]));
            }
            else if(obj instanceof String objStr) {
                string = objStr;
            }
            else {
                string = obj.toString();
            }

            this.messages.put(subkey, new ComponentEditor(string));
        }
    }
}
