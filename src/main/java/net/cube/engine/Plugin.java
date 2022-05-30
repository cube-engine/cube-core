package net.cube.engine;

/**
 * @author pluto
 * @date 2022/5/31
 */
public interface Plugin extends Named {

    String PLUGIN_FILE_PATH = "META-INF/services/cube/plugin";

    String PLUGIN_PROP_KEY = "class";

    String getType();

}
