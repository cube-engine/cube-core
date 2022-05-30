package net.cube.engine.impl;

import net.cube.engine.CubeRuntimeException;
import net.cube.engine.FileHelper;
import net.cube.engine.main.Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * @author pluto
 * @date 2022/5/16
 */
public class DefaultCodeSourceScanner extends AbstractCodeSourceScanner {
    public DefaultCodeSourceScanner(Object config) {
        super(config);
    }

    @Override
    public File[] scan() {
        Set<File> classFiles = new HashSet<>(Arrays.asList(super.scan()));
        final CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        final URL url = codeSource.getLocation();
        String protocolName = url.getProtocol();
        String codeSourceName;
        String sourcePath;
        if (protocolName.startsWith(FileHelper.PROTOCOL_JAR)) {
            try {
                codeSourceName = ((JarFile)url.getContent()).getName();
            } catch (IOException e) {
                throw new CubeRuntimeException(e);
            }
            File codeFile = new File(codeSourceName);
            sourcePath = codeFile.getParentFile().getAbsolutePath();
            if (sourcePath.indexOf("!/") > 0) {
                String originJarPath = sourcePath.substring(0, codeSourceName.indexOf("!"));
                File originJarFile = new File(originJarPath);
                String originParentPath = sourcePath.substring(codeSourceName.indexOf("!") + 1);
                if (originJarPath.startsWith("/")) {
                    originParentPath = originParentPath.substring(1);
                }
                File parentFile = unpackJarToTmpFolder(originJarFile, originParentPath);
                sourcePath = parentFile.getAbsolutePath();
            }
        } else if (protocolName.startsWith(FileHelper.PROTOCOL_FILE)) {
            codeSourceName = url.getPath();
            File codeFile = new File(codeSourceName);
            if (codeFile.isDirectory()) {
                sourcePath = codeFile.getAbsolutePath();
            } else {
                sourcePath = codeFile.getParentFile().getAbsolutePath();
            }
        } else {
            throw new CubeRuntimeException("No supported file type " + url.toString());
        }
        classFiles.addAll(Arrays.asList(Objects.requireNonNull(new File(sourcePath).listFiles())));
        File[] classFileArray = new File[classFiles.size()];
        classFiles.toArray(classFileArray);
        return classFileArray;
    }
}
