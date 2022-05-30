package net.cube.engine;

import java.io.File;

/**
 * @author pluto
 * @date 2022/5/16
 */
public interface CodeSourceScanner {

    String SYSTEM_JAVA_CLASS_PATH = "java.class.path";

    String SYSTEM_CLASS_DELIMITER = ":";

    String CODE_SOURCE_SCANNER_CONFIG_KEY = "scanner";

    File[] scan();

}
