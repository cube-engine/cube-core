package net.cube.engine.impl;

import net.cube.engine.CubeRuntimeException;
import net.cube.engine.Registry;

import java.util.Map;

/**
 * @author pluto
 * @date 2022/5/31
 */
public abstract class AbstractRegistry<T> implements Registry<T> {

    protected Map<String, Object> config;

    public AbstractRegistry(Map<String, Object> config) {
        this.config = config;
        init();
    }

    protected abstract void init() throws CubeRuntimeException;

}
