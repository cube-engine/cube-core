package net.cube.engine.manager;

import net.cube.engine.CodeSourceHandler;
import net.cube.engine.CubeRuntimeException;
import net.cube.engine.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author pluto
 * @date 2022/5/16
 */
public class PluginManager implements CodeSourceHandler {

    private static Logger LOG = LoggerFactory.getLogger(PluginManager.class);

    private final PluginRegistry registry;

    private static volatile PluginManager INSTANCE;

    private PluginManager() {
        registry = new PluginRegistry();
    }

    public static PluginManager getInstance() {
        if (INSTANCE == null) {
            synchronized (PluginManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PluginManager();
                }
            }
        }
        return INSTANCE;
    }

    private void loadPlugin(InputStream is) throws Exception {
        Properties properties = new Properties();
        properties.load(is);
        String className = properties.getProperty(Plugin.PLUGIN_PROP_KEY);
        LOG.info("[{}/loadPlugin] :: Current plugin class is {}.", this.getClass().getSimpleName(), className);
        Class<?> originClazz = Class.forName(className, false, this.getClass().getClassLoader());
        List<Class<?>> stack = new LinkedList<>();
        stack.add(0, originClazz);
        Class<?> clz;
        while (stack.size() > 0) {
            clz = stack.remove(0);
            if (Plugin.class.equals(clz)) {
                register(originClazz);
                stack.clear();
                break;
            } else {
                List<Class<?>> interfaces = Arrays.asList(clz.getInterfaces());
                if (interfaces.size() > 0) {
                    stack.addAll(0, interfaces);
                }
                clz = clz.getSuperclass();
                if (clz != null && !Object.class.equals(clz)) {
                    stack.add(clz);
                }
            }
        }
    }

    private void register(Class<?> clazz) throws Exception {
        Plugin plugin = (Plugin) clazz.newInstance();
        LOG.info("[{}/register] :: Plugin[{}] register.", this.getClass().getSimpleName(), plugin.getName());
        registry.put(plugin.getType(), plugin);
    }

    public Map<String, Plugin> getPluginByType(String type) {
        return registry.get(type);
    }

    @Override
    public void handleFile(File file) {
        if (file.getParentFile().getAbsolutePath().endsWith(Plugin.PLUGIN_FILE_PATH)) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                loadPlugin(fis);
                fis.close();
                fis = null;
            } catch (Exception e) {
                throw new CubeRuntimeException(e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void handleJar(JarFile file) {
        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!(entry.getName().contains(Plugin.PLUGIN_FILE_PATH) && !entry.isDirectory())) {
                continue;
            }
            try (InputStream is = file.getInputStream(entry)) {
                loadPlugin(is);
            } catch (Exception e) {
                throw new CubeRuntimeException(e);
            }
        }
    }

    class PluginRegistry {
        final Map<String, Map<String, Plugin>> registry;

        PluginRegistry() {
            registry = new ConcurrentHashMap<>(16);
        }

        void put(String group, Plugin plugin) {
            Map<String, Plugin> plugins = registry.computeIfAbsent(group, k -> new ConcurrentHashMap<>(16));
            if (plugins.containsKey(plugin.getName())) {
                LOG.debug("[{}/put] :: Plugin[{}] has exist in group[{}].", PluginRegistry.class.getSimpleName(),
                        plugin.getName(), group);
                return ;
            }
            plugins.put(plugin.getName(), plugin);
        }

        @SuppressWarnings("unchecked")
        Map<String, Plugin> get(String group) {
            Map<String, Plugin> plugins = registry.get(group);
            return plugins == null ? Collections.EMPTY_MAP : plugins;
        }
    }
}
