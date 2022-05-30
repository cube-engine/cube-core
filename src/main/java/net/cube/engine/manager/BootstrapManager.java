package net.cube.engine.manager;

import net.cube.engine.CodeSourceHandler;
import net.cube.engine.CubeRuntimeException;
import net.cube.engine.Launcher;
import net.cube.engine.annotation.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static net.cube.engine.Constant.FILE_PATH;
import static net.cube.engine.Constant.MANIFEST_ENTRY_NAME;

/**
 * @author pluto
 * @date 2022/5/16
 */
public class BootstrapManager implements CodeSourceHandler {

    private static Logger LOG = LoggerFactory.getLogger(BootstrapManager.class);

    private static final String FILE_EXTENSION_NAME = ".class";

    private final Map<Integer, List<Launcher>> registry = new HashMap<>(16);

    private static volatile BootstrapManager INSTANCE;

    private BootstrapManager() {
    }

    public static BootstrapManager getInstance() {
        if (INSTANCE == null) {
            synchronized (BootstrapManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BootstrapManager();
                }
            }
        }
        return INSTANCE;
    }

    public void handleFile(File file) {
        String absolutePath = file.getAbsolutePath();
        LOG.info("[{}/handleFile] :: Current class path is {}.", this.getClass().getSimpleName(), absolutePath);
        if (!absolutePath.endsWith(FILE_EXTENSION_NAME) || !absolutePath.contains(FILE_PATH)) {
            return;
        }
        int packageIndex = absolutePath.indexOf(FILE_PATH);
        String classPath = absolutePath.substring(packageIndex);
        handleClass(classPath);
    }

    public void handleJar(JarFile file) {
        Attributes manifestEntry;
        try {
            if (file.getManifest() == null) {
                return;
            }
            manifestEntry = file.getManifest().getMainAttributes();
        } catch (IOException e) {
            throw new CubeRuntimeException(e);
        }
        if (manifestEntry.getValue(MANIFEST_ENTRY_NAME) == null || "".equals(manifestEntry.getValue(MANIFEST_ENTRY_NAME))) {
            return;
        }
        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || !entry.getName().endsWith(FILE_EXTENSION_NAME)) {
                continue;
            }
            handleClass(entry.getName());
        }
    }

    private void handleClass(String classPath) {
        LOG.info("[{}/handleClass] :: Current classpath is {}.", this.getClass().getSimpleName(), classPath);
        int index = classPath.indexOf(FILE_EXTENSION_NAME);
        String className = classPath.substring(0, index).replace("/", ".");
        try {
            Class<?> clazz = Class.forName(className, false, this.getClass().getClassLoader());
            List<Class<?>> stack = new LinkedList<>();
            stack.add(0, clazz);
            boolean realized = false;
            Class<?> tempCls;
            while (stack.size() > 0) {
                tempCls = stack.remove(0);
                if (Launcher.class.equals(tempCls)) {
                    realized = true;
                    stack.clear();
                    break;
                } else {
                    List<Class<?>> interfaces = Arrays.asList(tempCls.getInterfaces());
                    if (interfaces.size() > 0) {
                        stack.addAll(0, interfaces);
                    }
                    tempCls = tempCls.getSuperclass();
                    if (tempCls != null && !Object.class.equals(tempCls)) {
                        stack.add(tempCls);
                    }
                }
            }
            if (!realized) {
                return;
            }
            Bootstrap annotation = clazz.getAnnotation(Bootstrap.class);
            if (annotation == null) {
                return;
            }
            Launcher launcher;
            if (annotation.singleton()) {
                Method method = clazz.getDeclaredMethod("getInstance");
                launcher = (Launcher)method.invoke(null);
            } else {
                launcher = (Launcher)clazz.newInstance();
            }
            Integer priority = annotation.priority();
            registry.computeIfAbsent(priority, k -> new LinkedList<>());
            registry.get(priority).add(launcher);
        } catch (Exception e) {
            throw new CubeRuntimeException(e);
        }
    }

    public void startAll() {
        List<Integer> keys = new LinkedList<>(registry.keySet());
        keys.sort(Integer::compareTo);
        keys.forEach(k -> {
            registry.get(k).forEach(launcher -> {
                try {
                    launcher.start();
                } catch (Exception e) {
                    throw new CubeRuntimeException(e);
                }
            });
        });
    }

}
