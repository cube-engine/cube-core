package net.cube.engine;

import java.util.Map;

/**
 * @author pluto
 * @date 2022/5/31
 */
public interface CubeUri {

    String getScheme();

    String getHostName();

    String getPath();

    Map<String, Map<String, String>> getPathParams();

    String getProtocol();

    Integer getPort();

    String getQuery();

    Map<String, String> getQueryParams();

    String getFragment();

    String getOrigin();

}