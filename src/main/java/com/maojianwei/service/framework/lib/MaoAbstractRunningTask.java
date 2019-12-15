package com.maojianwei.service.framework.lib;

import com.maojianwei.service.framework.core.MaoModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class MaoAbstractRunningTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private MaoAbstractModule module;
    public MaoAbstractRunningTask(MaoAbstractModule module) {
        this.module = module;
    }

    @Override
    public void run() {
        log.info("Injecting dependency for {} ...", module.name());
        try {
            injectDependency(module);
        } catch (InterruptedException e) {
            log.warn("INFO: exit required while injectDependency. Module: {}", module.name());
            return;
        }

        if (module.isNeedShutdown()) {
            log.info("Module need shutdown before activate {} ...", module.name());
            return;
        }

        log.info("Activating {} ...", module.name());
        module.activate();
        log.info("Activated {} ...", module.name());

        if (!module.isNeedShutdown()) {
            try {
                module.waitShutdown();
            } catch (InterruptedException e) {
                log.warn("InterruptedException when module.waitShutdown() for {}", module.name());
            }
        }

        log.info("Deactivating {} ...", module.name());
        module.deactivate();
        log.info("Closed {}.", module.name());
    }

    private void injectDependency(MaoAbstractModule module) throws InterruptedException {
        Class moduleClass = module.getClass();
        for (Field f : moduleClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(MaoReference.class)) {

                Class depClass = f.getType();
                if (MaoAbstractModule.class.isAssignableFrom(depClass)) {
                    f.setAccessible(true);

                    while (true) { // exit flag
                        MaoAbstractModule dep = MaoModuleManager.getInstance().getModule(depClass);
                        if (dep != null) {
                            try {
                                f.set(module, dep);
                            } catch (IllegalAccessException e) {
                                log.warn("IllegalAccessException for class {}", moduleClass.getName());
                            }
                            break;
                        } else {
                            Thread.sleep(500);
                        }
                    }
                } else {
                    log.warn("class {} field {} is not a module", moduleClass.getName(), f.getName());
                }
            }
        }
    }
}
