package com.maojianwei.service.framework.core;

import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoAbstractRunningTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MaoRunningCore {

    private ExecutorService runningPool;

    private static MaoRunningCore singletonInstance;
    public static MaoRunningCore getInstance() {
        if (singletonInstance == null) {
            synchronized (MaoRunningCore.class) {
                if (singletonInstance == null) {
                    singletonInstance = new MaoRunningCore();
                }
            }
        }
        return singletonInstance;
    }

    private void initPool() {
        if (runningPool == null) {
            synchronized(MaoRunningCore.class) {
                if (runningPool == null) {
                    runningPool = Executors.newCachedThreadPool();
                }
            }
        }
    }

    public void startPool() {
        initPool();
    }

    public void runModule(MaoAbstractModule module) {
        runningPool.submit(new MaoAbstractRunningTask(module));
    }
}
