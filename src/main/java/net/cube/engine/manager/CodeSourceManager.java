package net.cube.engine.manager;

import net.cube.engine.CodeSourceHandler;
import net.cube.engine.CodeSourceScanner;
import net.cube.engine.ConfigHelper;
import net.cube.engine.Configurable;
import net.cube.engine.CubeRuntimeException;
import net.cube.engine.FileHelper;
import net.cube.engine.Launcher;
import net.cube.engine.ObjectHelper;
import net.cube.engine.impl.DefaultCodeSourceScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

import static net.cube.engine.Constant.DOT;

/**
 * @author pluto
 * @date 2022/5/16
 */
public class CodeSourceManager implements Configurable, Launcher {

    private static Logger LOG = LoggerFactory.getLogger(CodeSourceManager.class);

    private static final String CODE_SOURCE_HANDLER_KEY = "handlers";

    protected CodeSourceScanner scanner;

    protected volatile File[] codeFiles;

    private static volatile CodeSourceManager INSTANCE;

    protected final List<CodeSourceHandler> handlers;

    private CodeSourceManager() {
        handlers = new LinkedList<>();
    }

    public static CodeSourceManager getInstance() {
        if (INSTANCE == null) {
            synchronized (CodeSourceManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CodeSourceManager();
                    INSTANCE.init();
                }
            }
        }
        return INSTANCE;
    }

    private void init() {
        Object config = ConfigHelper.getInstance().getConfig(getConfigKey());
        String className = null;
        if (config != null) {
            className = ObjectHelper.getProperty(config, CodeSourceScanner.CODE_SOURCE_SCANNER_CONFIG_KEY, String.class);
        }
        if (className == null) {
            className = DefaultCodeSourceScanner.class.getName();
        }
        try {
            Class<?> clazz = Class.forName(className, false, this.getClass().getClassLoader());
            Constructor<?> constructor = clazz.getConstructor(Object.class);
            this.scanner = (CodeSourceScanner) constructor.newInstance(config);
            Collection<String> collection = ObjectHelper.toCollection(ObjectHelper.getProperty(config, CODE_SOURCE_HANDLER_KEY));
            for (String handlerClassName : collection) {
                Class<?> handlerClazz = Class.forName(handlerClassName, false, this.getClass().getClassLoader());
                Object obj = handlerClazz.newInstance();
                if (obj instanceof CodeSourceHandler) {
                    CodeSourceManager.getInstance().addHandlers((CodeSourceHandler)obj);
                }
            }
            CodeSourceManager.getInstance().addHandlers(BootstrapManager.getInstance(), PluginManager.getInstance());
        } catch (Exception e) {
            throw new CubeRuntimeException(e);
        }
    }

    public void handle(File file) throws CubeRuntimeException {
        if (file == null) {
            return ;
        }
        if (file.isDirectory()) {
            handleFile(file);
        } else {
            String name = file.getName();
            String extensionName = name.substring(name.lastIndexOf(DOT) + 1);
            if (!FileHelper.PROTOCOL_JAR.equalsIgnoreCase(extensionName)) {
                return ;
            }
            JarFile jarFile;
            try {
                jarFile = new JarFile(file);
            } catch (IOException e) {
                throw new CubeRuntimeException(e);
            }
            handleJar(jarFile);
        }
    }

    private void handleFile(File file) {
        List<File> workStack = new LinkedList<>();
        workStack.add(file);
        File curFile;
        do {
            curFile = workStack.remove(0);
            if (curFile.isDirectory() && curFile.listFiles().length > 0) {
                workStack.addAll(0, Arrays.asList(curFile.listFiles()));
                continue ;
            }
            if (handlers.size() > 0) {
                for (CodeSourceHandler handler : handlers) {
                    handler.handleFile(curFile);
                }
            }
        } while (workStack.size() != 0);
    }

    private void handleJar(JarFile file) {
        if (handlers.size() > 0) {
            for (CodeSourceHandler handler : handlers) {
                handler.handleJar(file);
            }
        }
    }

    public void addHandlers(CodeSourceHandler... handlers) {
        this.handlers.addAll(Arrays.asList(handlers));
    }

    @Override
    public String getConfigKey() {
        return CodeSourceManager.class.getSimpleName();
    }

    @Override
    public void start() throws Exception {
        codeFiles = scanner.scan();
        for (File f : codeFiles) {
            CodeSourceManager.getInstance().handle(f);
        }
    }

    @Override
    public void stop() throws Exception {
        //Nothing to do
    }

    public CodeSourceScanner getScanner() {
        return scanner;
    }

    public File[] getCodeFiles() {
        return codeFiles;
    }

}
