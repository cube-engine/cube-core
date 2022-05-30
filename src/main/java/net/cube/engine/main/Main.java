package net.cube.engine.main;

import net.cube.engine.ConfigHelper;
import net.cube.engine.manager.BootstrapManager;
import net.cube.engine.manager.CodeSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pluto
 * @date 2022/5/16
 */
public class Main {

    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static final String DEFAULT_FRAMEWORK_CONFIG_PATH = "META-INF/cube.yaml";

    private Main() {
    }

    public static void run(String... args) {
        disassemblyArgs(args);
        startup();
    }

    private static void disassemblyArgs(String[] args) {
        System.setProperty(ConfigHelper.CONFIG_FILE_NAME_PROP, DEFAULT_FRAMEWORK_CONFIG_PATH);
    }

    private static void startup() {
        try {
            CodeSourceManager.getInstance().start();
            BootstrapManager.getInstance().startAll();
            LOG.info("Engine has been started successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR :: Engine startup error. \n\rERROR :: " + e);
            System.exit(-1);
        }
    }

}
