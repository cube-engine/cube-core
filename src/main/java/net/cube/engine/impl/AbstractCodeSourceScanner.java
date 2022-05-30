package net.cube.engine.impl;

import net.cube.engine.CodeSourceScanner;
import net.cube.engine.CubeRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author pluto
 * @date 2022/5/16
 */
public abstract class AbstractCodeSourceScanner implements CodeSourceScanner {

    protected static Logger LOG = LoggerFactory.getLogger(AbstractCodeSourceScanner.class);

    public static final String JAVA_TEMP_DIR = "java.io.tmpdir";

    protected Object config;

    public AbstractCodeSourceScanner(Object config) {
        this.config = config;
    }

    public File[] scan() {
        String javaClassPath = System.getProperty(SYSTEM_JAVA_CLASS_PATH);
        if (javaClassPath == null || "".equals(javaClassPath)) {
            return new File[0];
        }
        String[] classFilePaths = javaClassPath.split(SYSTEM_CLASS_DELIMITER);
        List<File> classFiles = new ArrayList<>(classFilePaths.length);
        for (String classFilePath : classFilePaths) {
            classFiles.add(new File(classFilePath));
        }
        File[] classFileArray = new File[classFiles.size()];
        classFiles.toArray(classFileArray);
        return classFileArray;
    }

    protected File unpackJarToTmpFolder(File originJarFile, String originParentPath) {
        try {
            LOG.info("{}/unpackJarToTempFolder :: Current origin path is {}",
                    this.getClass().getSimpleName(), originParentPath);
            File tempFolder = new File(System.getProperty(JAVA_TEMP_DIR));
            File folder = new File(tempFolder, originJarFile.getName() + "-engine-libs-" + UUID.randomUUID());
            folder.deleteOnExit();
            if (!folder.mkdir()) {
                throw new CubeRuntimeException("Can not create temp dir.");
            }
            JarFile jarFile = new JarFile(originJarFile);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String entryAbsoluteName = jarEntry.getName();
                String entryParentName = entryAbsoluteName.substring(0, entryAbsoluteName.lastIndexOf("/"));
                String entryName = entryAbsoluteName.substring(entryAbsoluteName.lastIndexOf("/") + 1);
                if (entryParentName.equalsIgnoreCase(originParentPath) && !"".equals(entryName)) {
                    File f = new File(folder, entryName);
                    try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                        try (OutputStream outputStream = new FileOutputStream(f)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            outputStream.flush();
                        }
                    }
                    f.deleteOnExit();
                }
            }
            return folder;
        } catch (Exception e) {
            throw new CubeRuntimeException(e);
        }
    }
}
