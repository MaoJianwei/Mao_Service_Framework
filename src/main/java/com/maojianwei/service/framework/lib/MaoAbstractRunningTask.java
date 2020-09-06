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
        try {
            log.info("Injecting dependency for {} ...", module.name());
            injectDependency(module);
        } catch (InterruptedException e) {
            log.warn("exit required while injectDependency. Module: {}", module.name());
            return;
        }

        if (module.isNeedShutdown()) {
            log.info("Module need shutdown before activate 1, {} ...", module.name());
            return;
        }

        log.info("Wait dependencies ready, {} ...", module.name());
        waitDependencyFinishActivating(module);

        if (module.isNeedShutdown()) {
            log.info("Module need shutdown before activate 2, {} ...", module.name());
            return;
        }

        try {
            log.info("Activating {} ...", module.name());
            module.activate();
            log.info("Activated {} ...", module.name());
        } catch (Exception e) {
            log.error("Activating exception: {}", e.toString());
            return;
        }

        if (!module.isNeedShutdown()) {
            try {
                module.waitShutdown();
            } catch (InterruptedException e) {
                log.warn("InterruptedException when module.waitShutdown() for {}", module.name());
            }
        }

        try {
            log.info("Deactivating {} ...", module.name());
            module.deactivate();
            log.info("Closed {}.", module.name());
        } catch (Exception e) {
            log.error("Deactivate exception: {}", e.toString());
        }
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

    private void waitDependencyFinishActivating(MaoAbstractModule module) {
        Class moduleClass = module.getClass();
        for (Field f : moduleClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(MaoReference.class)) {
                try {
                    f.setAccessible(true);
                    MaoAbstractModule dependency = (MaoAbstractModule) f.get(module);
                    log.info("Dependency waiting {} -> {}", module.name(), dependency.name());
                    while(!dependency.readyNow()) {
                        Thread.sleep(200);
                    }
                    log.info("Dependency ready {} -> {}", module.name(), dependency.name());
                } catch (InterruptedException e) {
                    break;
                } catch (IllegalAccessException | IllegalArgumentException |
                         NullPointerException | ExceptionInInitializerError e) {
                    log.error("You should only annotate MaoAbstractModule's child with MaoReference, {}, ignoring: {}, exception: {}", module.name(), f.toString(), e.toString());
                } catch (Exception e) {
                    log.error("Unknown exception while waiting dependency, {}, {}", module.name(), e.toString());
                }
            }
        }
    }
}
