package net.cube.engine.manager;

import net.cube.engine.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author pluto
 * @date 2022/5/16
 */
public abstract class AbstractManager implements Manager {

    protected static Logger LOG = LoggerFactory.getLogger(AbstractManager.class);

    protected volatile boolean started;

    protected Lock lock = new ReentrantLock();

    @Override
    public void start() throws Exception {
        if (started) {
            LOG.warn("{} has been started. Do not startup again.", this.getClass().getSimpleName());
            return ;
        }
        started : if (lock.tryLock()) {
            try {
                if (started) {
                    break started;
                }
                runStartup();
                started = true;
                LOG.info("{} is started successfully.", this.getClass().getSimpleName());
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     *
     * @throws Exception
     */
    protected abstract void runStartup() throws Exception;

    @Override
    public void stop() throws Exception {
        if (!started) {
            LOG.warn("{} has been stopped. Do not shutdown again.", this.getClass().getSimpleName());
            return ;
        }
        stopped : if (lock.tryLock()) {
            try {
                if (!started) {
                    break stopped;
                }
                started = false;
                runShutdown();
                LOG.info("{} is stopped successfully.", this.getClass().getSimpleName());
            } finally {
                lock.unlock();
            }
        }
    }

    protected abstract void runShutdown() throws Exception;
}
