package com.github.kuramastone.permissionTrial.utils;

import com.github.kuramastone.permissionTrial.PermissionTrial;
import org.apache.logging.log4j.LogManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;

/**
 * Custom yaml manager used by KuramaStone across many platforms.
 */
public class YamlConfig {


    private @Nullable String parentKeyName;
    private @Nullable String fullParentKey;
    private LinkedHashMap<String, Object> yamlMap;
    private Yaml yaml;

    private Path path;
    private String parentPath; // used for finding the resource in jar

    private Logger logger;

    public YamlConfig(File alternativeFolder, String fileName) {
        this(alternativeFolder, null, fileName);
    }

    public YamlConfig(File directFile) {
        this(directFile.getParentFile(), "", directFile.getName());
    }

    public YamlConfig(File parentFolder, String parentPath, String fileName) {
        path = new File((parentPath == null || parentPath.isEmpty()) ? parentFolder : new File(parentFolder, parentPath), fileName).toPath();
        this.parentPath = parentPath;
        logger = PermissionTrial.logger;
        if (logger == null) {
            LogManager.getLogger(getClass());
        }


        loadYaml();
    }

    private YamlConfig(String fullParentKey, LinkedHashMap<String, Object> yamlMap) {
        this.yamlMap = yamlMap;

        if (fullParentKey != null) {
            String[] sections = fullParentKey.split("\\.");
            this.fullParentKey = fullParentKey;
            this.parentKeyName = sections[sections.length - 1];
        }

        logger = PermissionTrial.logger;
        if (logger == null) {
            LogManager.getLogger(getClass());
        }
    }

    public boolean containsKey(String key) {

        String[] sections = key.split("\\.");

        Map<String, Object> parent = yamlMap;
        for (int i = 0; i < sections.length; i++) {
            String sec = sections[i];
            Object obj = parent.get(sec);

            if (obj == null) {
                if (i == sections.length - 1) {
                    return false;
                }

                throw new RuntimeException(String.format("Object %s in %s has a null value and cannot be mapped.", sec, key));
            }

            if (isObjectASectionMap(obj)) {
                parent = (Map<String, Object>) obj;
            }

            // if object exists at final key, return true
            if (i == sections.length - 1) {
                return true;
            }

        }

        return false;
    }

    public <T> T get(String key, T def) {
        if (!hasKey(key)) {
            return def;
        }

        String[] sections = key.split("\\.");

        Map<String, Object> parent = yamlMap;
        for (int i = 0; i < sections.length; i++) {
            String sec = sections[i];
            Object obj = parent.get(sec);

            if (obj == null) {
                if (i == sections.length - 1) {
                    return def;
                }

                throw new RuntimeException(String.format("Object at %s has a null value and cannot be retrieved. This path doesnt exist...", key));
            }

            if (isObjectASectionMap(obj)) {
                parent = (Map<String, Object>) obj;
            }

            if (i == sections.length - 1) {
                return (T) obj;
            }

        }

        return def;
    }


    /**
     * Sets a value in the YAML map based on a dot-separated path.
     *
     * @param key   The path to the key (e.g., "a.4.p").
     * @param value The value to set.
     * @param <T>   The type of the value.
     */
    public <T> void set(String key, T value) {
        String[] sections = key.split("\\.");
        Map<String, Object> currentMap = yamlMap;

        // Traverse the path, creating nested maps as needed
        for (int i = 0; i < sections.length - 1; i++) {
            String section = sections[i];
            // Check if the current section is a map
            if (currentMap.containsKey(section) && currentMap.get(section) instanceof Map) {
                // If it's a map, cast it to Map<String, Object>
                currentMap = (Map<String, Object>) currentMap.get(section);
            }
            else {
                // If it isn't a map or doesn't exist, create a new map
                Map<String, Object> newMap = createEmptySection();
                currentMap.put(section, newMap);
                currentMap = newMap;
            }
        }

        // Set the final value in the last map
        String finalKey = sections[sections.length - 1];
        currentMap.put(finalKey, value);
    }

    public List<String> getKeys(String path, boolean deep) {
        List<String> keys = new ArrayList<>();

        LinkedHashMap<String, Object> map = path.isEmpty() ? yamlMap : get(path, createEmptySection());
        Objects.requireNonNull(map);


        StringBuilder keyPrefix = new StringBuilder("");
        collectKeys(keys, map, deep, keyPrefix);

        /*
        Remove trailing period
         */
        List<String> modifiedKeys = new ArrayList<>(keys.size());
        for (String string : keys) {
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
            modifiedKeys.add(string);
        }

        return modifiedKeys;
    }

    private void collectKeys(List<String> keys, Map<String, Object> map, boolean deep, StringBuilder keyPrefix) {

        for (Map.Entry<String, Object> set : map.entrySet()) {
            keys.add(keyPrefix + set.getKey() + ".");

            if (deep && isObjectASectionMap(set.getValue())) {
                collectKeys(keys, (Map<String, Object>) set.getValue(), deep, new StringBuilder(keyPrefix + set.getKey() + "."));
            }

        }

    }

    public YamlConfig getSection(String key) {
        String[] sections = key.split("\\.");
        LinkedHashMap<String, Object> parent = yamlMap;
        Object lastObject = null;
        for (int i = 0; i < sections.length; i++) {
            String sec = sections[i];
            Object obj = parent.get(sec);

            if (obj == null) {
                return null;
            }
            lastObject = obj;

            if (isObjectASectionMap(obj)) {
                parent = (LinkedHashMap<String, Object>) obj;
            }

        }

        if (!isObjectASectionMap(lastObject)) {
            return null;
        }

        // build full parent path key with this key appended.
        StringBuilder fullKey = new StringBuilder();
        if (this.fullParentKey != null) {
            fullKey.append(this.fullParentKey).append(".");
        }
        fullKey.append(key);

        return new YamlConfig(fullKey.toString(), (LinkedHashMap<String, Object>) lastObject);
    }

    public Object getObject(String key) {
        if (hasKey(key)) {
            return get(key, (Object) null);
        }
        throw new RuntimeException(String.format("Unable to find key '%s'.", key));
    }

    public int getInt(String key) {
        if (hasKey(key)) {
            return get(key, 0);
        }
        throw new RuntimeException(String.format("Unable to find key '%s'.", key));
    }

    public int getKeys(String key) {
        if (hasKey(key)) {
            return get(key, 0);
        }
        throw new RuntimeException(String.format("Unable to find key '%s'.", key));
    }

    public String getString(String key) {
        if (hasKey(key)) {
            return get(key, "");
        }
        throw new RuntimeException(String.format("Unable to find key '%s'.", key));
    }

    public ArrayList<String> getStringList(String key) {
        if (hasKey(key)) {
            return get(key, new ArrayList<String>());
        }
        throw new RuntimeException(String.format("Unable to find key '%s'.", key));
    }

    public double getDouble(String key) {
        if (hasKey(key)) {
            return get(key, 0.0D);
        }
        throw new RuntimeException(String.format("Unable to find key '%s'.", key));
    }

    public boolean getBoolean(String key) {
        if (hasKey(key)) {
            return get(key, false);
        }
        throw new RuntimeException(String.format("Unable to find key '%s'.", key));
    }

    public boolean isObjectASectionMap(Object key) {
        return key != null && key instanceof Map;
    }

    public LinkedHashMap<String, Object> getYamlMap() {
        return yamlMap;
    }

    public boolean hasKey(String key) {
        String[] sections = key.split("\\.");
        Map<String, Object> parent = yamlMap;
        for (int i = 0; i < sections.length; i++) {
            String sec = sections[i];
            Object obj = parent.get(sec);

            if (obj == null) {
                return false;
            }

            if (i == sections.length - 1) {
                return true; // The key exists
            }

            if (isObjectASectionMap(obj)) {
                parent = (Map<String, Object>) obj;
            }
            else {
                return false; // The next child key has nowhere to check next. no more children exist, so return false.
            }

        }

        return true;
    }

    @Nullable
    public String getParentKeyName() {
        return parentKeyName;
    }

    private void loadYaml() {
        if (path == null) {
            throw new RuntimeException("Cannot load a subsection of a yaml config. Try again using the root.");
        }

        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag -> true);

        yaml = new Yaml(new CustomConstructor(loaderOptions), new Representer(createOptions()), createOptions());

        if (getFile().exists()) {
            try (InputStream in = Files.newInputStream(path)) {
                // Load YAML as a Map
                yamlMap = yaml.load(in);

                // could be empty
                if (yamlMap == null) {
                    yamlMap = createEmptySection();
                }
                else {
                    processKeys(yamlMap);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            saveAndLoadFromJar();
        }

        if (yamlMap == null) {
            throw new RuntimeException("Unable to load any data.");
        }

    }

    /**
     * T
     */
    public void saveAndLoadFromJar() {
        File file = path.toFile();

        file.getParentFile().mkdirs();
        String resourcePath = (parentPath == null || parentPath.isEmpty()) ?
                "/" + file.getName() :
                "/" + parentPath + "/" + file.getName();
        try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
            if (resourceStream != null) {
                // Copy resource from JAR to file on disk
                Files.copy(resourceStream, path, StandardCopyOption.REPLACE_EXISTING);
                logger.info(String.format("Found default data for %s in the jar file. Pasting to path!", resourcePath));

                loadYaml();

            }
            else {
                // If the file is not in the JAR, create an empty YAML file
                file.createNewFile();
                logger.warning(String.format("Unable to find default data for %s in the jar file. Making new empty file.", resourcePath));
                yamlMap = createEmptySection();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Could not create or copy yaml file %s", resourcePath), e);
        }
    }

    private static LinkedHashMap<String, Object> createEmptySection() {
        return new LinkedHashMap<>();
    }

    /**
     * Forces the Map<String, Object> structure. When loading from a yaml, it can sometimes contain Map<Integer, Object>
     *
     * @param map
     */
    private void processKeys(Map map) {
        // ensure the key of each map is a string

        Map actual = new HashMap<>(map.size());
        for (Object key : map.keySet()) {
            Object value = map.get(key);

            if (value instanceof Map) {
                processKeys((Map) value);
            }

            actual.put(String.valueOf(key), value);

        }

        map.clear();
        map.putAll(actual);
    }


    public void save() {
        if (path == null) {
            throw new RuntimeException("Cannot saveAll a subsection of a yaml config. Try again using the root.");
        }


        // Write the object to a YAML file
        try (FileWriter writer = new FileWriter(path.toFile())) {
            yaml.dump(yamlMap, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String removeLastKey(String input) {
        // Split the string by the dot (.)
        String[] parts = input.split("\\.");

        // Check if there is more than one part
        if (parts.length == 0) {
            return input; // No change if the input is empty or has no dots
        }

        // Join the remaining parts excluding the last one
        String[] remainingParts = new String[parts.length - 1];
        System.arraycopy(parts, 0, remainingParts, 0, parts.length - 1);

        // Rejoin the parts with dots
        return String.join(".", remainingParts);
    }

    private DumperOptions createOptions() {
        if (path == null) {
            throw new RuntimeException("Cannot modify options for subsection of a yaml config. Try again using the root.");
        }

        // Create DumperOptions to customize the YAML output
        DumperOptions options = new DumperOptions();
        options.setIndent(2); // Set the number of spaces for indentation
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Use block style for complex structures
        options.setPrettyFlow(true); // Enable pretty
        options.setProcessComments(true);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);

        return options;
    }

    public File getFile() {
        return path.toFile();
    }

    public Path getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YamlConfig that = (YamlConfig) o;
        return Objects.equals(yamlMap, that.yamlMap) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(yamlMap, path);
    }


    @Override
    public String toString() {
        return "YamlConfigLoader{" +
                "path=" + path +
                ", yamlMap=" + yamlMap +
                '}';
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public static File defaultDataFolder() {
        return PermissionTrial.instance.getDataFolder();
    }

    public static <T> T loadFromYaml(T object, YamlConfig section) {
        return loadFromYaml(object, section, null);
    }

    /**
     * @param object  Object to insert data into
     * @param section Section from which to load the data
     * @param mapper  Used to read certain types differently, particularly maps but also generic classes.
     * @param <T>
     * @return
     */
    public static <T> T loadFromYaml(T object, YamlConfig section, @Nullable Mapper mapper) {
        try {
            Class<?> clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(YamlKey.class)) {
                    YamlKey yamlKey = field.getAnnotation(YamlKey.class);
                    String key = yamlKey.value();

                    // Set field value from YAML if present
                    field.setAccessible(true);
                    Object value = Mapper.EMPTY;

                    // first let mapper try
                    if (mapper != null) {
                        value = mapper.mapFrom(object, key, section.getSection(key));
                    }

                    // if mapper didnt override, try loading the value inside the yaml
                    if (value == Mapper.EMPTY & section.containsKey(key)) {
                        value = section.getObject(key);
                    }

                    // if the yaml didnt have it, try using the default value of the field
                    if (value == Mapper.EMPTY) { // if null, try to use default value
                        value = field.get(object);
                    }

                    // check if it is permitted to be null
                    boolean isNullable = (field.getAnnotation(Nullable.class) != null);

                    if (!isNullable) {
                        Objects.requireNonNull(value, String.format("Cannot load key '%s' with null value for field '%s'. It was not overriden, not found in yaml, and had no default field value.", key, field.getName()));
                    }

                    field.set(object, value);
                    field.setAccessible(false);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return object;
    }

    public static File[] getYamlFiles(File directory) {
        // Check if the provided File is a directory
        if (directory.isDirectory()) {
            // Use FilenameFilter to filter for .yml and .yaml files
            FilenameFilter yamlFilter = (dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml");
            return directory.listFiles(yamlFilter); // Get an array of YAML files
        }
        else {
            throw new RuntimeException(String.format("Cannnot search for yaml files in a non-directory '%s'.", directory.getAbsoluteFile()));
        }
    }

    /**
     * Limited utility. Setters and getters will work, but it cannot saveAll without a file.
     *
     * @param data
     * @return
     */
    public static YamlConfig direct(LinkedHashMap<String, Object> data) {
        return new YamlConfig(null, data);
    }

    public boolean isSection(String key) {
        return getObject(key) instanceof Map;
    }

    /**
     * Only copies data and not file information. Cannot saveAll but will still contain the same data.
     *
     * @return
     */
    public YamlConfig shallowCopy() {
        return YamlConfig.direct(shallowCopyYamlMap(this.yamlMap));
    }

    private static LinkedHashMap<String, Object> shallowCopyYamlMap(LinkedHashMap<String, Object> map) {

        LinkedHashMap<String, Object> copy = createEmptySection();
        for (Map.Entry<String, Object> set : map.entrySet()) {
            Object value = set.getValue();
            if (value instanceof LinkedHashMap) {
                value = shallowCopyYamlMap((LinkedHashMap<String, Object>) value);
            }
            copy.put(set.getKey(), value);
        }

        return copy;
    }

    public void clear() {
        this.yamlMap.clear();
    }

    public YamlConfig getOrCreateSection(String key) {
        YamlConfig section = getSection(key);

        if (section == null) {
            set(key, createEmptySection());
        }

        return getSection(key);
    }


    public void installNewKeysFromDefault(String fullParentKey, boolean deep) {
        File yamlFile = this.path.toFile();

        String resourcePath = yamlFile.getName();
        boolean replace = false;
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');

            InputStream in = PermissionTrial.instance.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found.");
            }
            else {

                try {
                    boolean wasUpdated = false;
                    // try saving new keys if they exist in the jar but not the file
                    YamlConfiguration resYaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in));

                    for (String key : resYaml.getKeys(deep)) {
                        if (!key.startsWith(fullParentKey)) {
                            continue;
                        }

                        // load each new key. if it doesnt exist in the file, saveAll the whole section here
                        if (!this.containsKey(key)) {
                            wasUpdated = true;

                            logger.warning("%s did not contain key '%s'. Inserting default value.'"
                                    .formatted(yamlFile.getName(), key));
                            this.set(key, resYaml.get(key));
                        }

                    }

                    if (wasUpdated) {
                        logger.info("Updated config " + yamlFile.getName() + " with new settings!");

                        // reload. this ensures the proper loading format is preserved
                        save();
                        loadYaml();
                    }

                }
                catch (Exception var10) {
                    Exception ex = var10;
                    logger.severe("Could not update config " + yamlFile.getName() + " with new settings.");
                    ex.printStackTrace();
                }

            }
        }
        else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }

    /**
     * Used when parsing keys for YamlConfig loaders. This lets you ignore the default loading mechanism and insert your own object no automatically loadable by Yaml.
     */
    public static interface Mapper {

        UUID EMPTY = UUID.fromString("d03d0e68-6d68-4d5f-8f25-e599b58cadf5");

        Object mapFrom(Object obj, String key, YamlConfig section);

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface YamlKey {
        String value();
    }

    /**
     * Loads items as a LinkedHashMap to keep them in order. So much code because a dev didnt want to make things public access for some reason. why.
     */
    public static class CustomConstructor extends SafeConstructor {

        public CustomConstructor(LoaderOptions loaderOptions) {
            super(loaderOptions);
        }

        protected void flattenMapping(MappingNode node, boolean forceStringKeys) {
            this.processDuplicateKeys(node, forceStringKeys);
            if (node.isMerged()) {
                node.setValue(mergeNode(node, true, new LinkedHashMap(), new ArrayList(), forceStringKeys));
            }

        }

        private List<NodeTuple> mergeNode(MappingNode node, boolean isPreffered, Map<Object, Integer> key2index, List<NodeTuple> values, boolean forceStringKeys) {
            Iterator<NodeTuple> iter = node.getValue().iterator();

            while (true) {
                label39:
                while (iter.hasNext()) {
                    NodeTuple nodeTuple = (NodeTuple) iter.next();
                    Node keyNode = nodeTuple.getKeyNode();
                    Node valueNode = nodeTuple.getValueNode();
                    if (keyNode.getTag().equals(Tag.MERGE)) {
                        iter.remove();
                        switch (valueNode.getNodeId()) {
                            case mapping:
                                MappingNode mn = (MappingNode) valueNode;
                                this.mergeNode(mn, false, key2index, values, forceStringKeys);
                                break;
                            case sequence:
                                SequenceNode sn = (SequenceNode) valueNode;
                                List<Node> vals = sn.getValue();
                                Iterator var13 = vals.iterator();

                                while (true) {
                                    if (!var13.hasNext()) {
                                        continue label39;
                                    }

                                    Node subnode = (Node) var13.next();
                                    if (!(subnode instanceof MappingNode)) {
                                        throw new RuntimeException("while constructing a mapping "+ node.getStartMark()+ "  expected a mapping for merging, but found " + subnode.getNodeId() +", subnode start: " + subnode.getStartMark());
                                    }

                                    MappingNode mnode = (MappingNode) subnode;
                                    this.mergeNode(mnode, false, key2index, values, forceStringKeys);
                                }
                            default:
                                throw new RuntimeException("while constructing a mapping " + node.getStartMark() + " expected a mapping or list of mappings for merging, but found " + valueNode.getNodeId() + ", value start: " + valueNode.getStartMark());
                        }
                    }
                    else {
                        if (forceStringKeys) {
                            if (!(keyNode instanceof ScalarNode)) {
                                throw new YAMLException("Keys must be scalars but found: " + keyNode);
                            }

                            keyNode.setType(String.class);
                            keyNode.setTag(Tag.STR);
                        }

                        Object key = this.constructObject(keyNode);
                        if (!key2index.containsKey(key)) {
                            values.add(nodeTuple);
                            key2index.put(key, values.size() - 1);
                        }
                        else if (isPreffered) {
                            values.set((Integer) key2index.get(key), nodeTuple);
                        }
                    }
                }

                return values;
            }
        }

        protected void processDuplicateKeys(MappingNode node) {
            this.processDuplicateKeys(node, false);
        }

        protected void processDuplicateKeys(MappingNode node, boolean forceStringKeys) {
            List<NodeTuple> nodeValue = node.getValue();
            Map<Object, Integer> keys = new LinkedHashMap(nodeValue.size());
            TreeSet<Integer> toRemove = new TreeSet();
            int i = 0;

            Iterator indices2remove;
            for (indices2remove = nodeValue.iterator(); indices2remove.hasNext(); ++i) {
                NodeTuple tuple = (NodeTuple) indices2remove.next();
                Node keyNode = tuple.getKeyNode();
                if (!keyNode.getTag().equals(Tag.MERGE)) {
                    if (forceStringKeys) {
                        if (!(keyNode instanceof ScalarNode)) {
                            throw new YAMLException("Keys must be scalars but found: " + keyNode);
                        }

                        keyNode.setType(String.class);
                        keyNode.setTag(Tag.STR);
                    }

                    Object key = this.constructObject(keyNode);
                    if (key != null && !forceStringKeys && keyNode.isTwoStepsConstruction()) {
                        if (!this.loadingConfig.getAllowRecursiveKeys()) {
                            throw new YAMLException("Recursive key for mapping is detected but it is not configured to be allowed.");
                        }

                        try {
                            key.hashCode();
                        }
                        catch (Exception var12) {
                            Exception e = var12;
                            throw new RuntimeException("while constructing a mapping " + node.getStartMark() + " found unacceptable key " + key + " " + tuple.getKeyNode().getStartMark(), e);
                        }
                    }

                    Integer prevIndex = (Integer) keys.put(key, i);
                    if (prevIndex != null) {
                        if (!this.isAllowDuplicateKeys()) {
                            throw new RuntimeException("while constructing a mapping " + node.getStartMark() + " found duplicate key " + key + ": " + tuple.getKeyNode().getStartMark());
                        }

                        toRemove.add(prevIndex);
                    }
                }
            }

            indices2remove = toRemove.descendingIterator();

            while (indices2remove.hasNext()) {
                nodeValue.remove((Integer) indices2remove.next());
            }

        }

    }

}