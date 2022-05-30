package net.cube.engine;

import java.util.Collection;
import java.util.List;

/**
 * @author pluto
 * @date 2022/5/31
 */
public interface Registry<T> {

    T get(String tenant, String key, String... index);

    Collection<T> getAll();

    List<T> getAll(String tenant, String... index);

    boolean set(String tenant, String key, T t, boolean isImmutable, String... index);

    Long remove(String tenant, String key, String... index);

}
