package net.cube.engine;

import java.io.File;
import java.util.jar.JarFile;

/**
 * @author pluto
 * @date 2022/5/16
 */
public interface CodeSourceHandler {

    void handleFile(File file);

    void handleJar(JarFile file);

}
