package com.maojianwei.service.framework.lib;

import com.maojianwei.service.framework.core.MaoModuleManager;

import java.lang.reflect.Field;

public class MaoAbstractRunningTask implements Runnable {

    private MaoAbstractModule module;

    public MaoAbstractRunningTask(MaoAbstractModule module) {
        this.module = module;
    }

    @Override
    public void run() {
        System.out.println(String.format("Injecting dependency for %s ...", module.name()));
        try {
            injectDependency(module);
        } catch (InterruptedException e) {
            System.out.println(String.format("INFO: exit required while injectDependency. Module: %s", module.name()));
            return;
        }

        if (module.isNeedShutdown()) {
            return;
        }

        //TODO - Check exit flag!

        System.out.println(String.format("Activating %s ...", module.name()));
        module.activate();
        System.out.println(String.format("Activated %s ...", module.name()));

        if (!module.isNeedShutdown()) {
            try {
                module.waitShutdown();
            } catch (InterruptedException e) {
                System.out.println(String.format("InterruptedException when module.waitShutdown() for %s", module.name()));
            }
        }

        System.out.println(String.format("Deactivating %s ...", module.name()));
        module.deactivate();
        System.out.println(String.format("Closed %s .", module.name()));
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
                                System.out.println(String.format("WARN: IllegalAccessException for class %s", moduleClass.getName()));
                            }
                            break;
                        } else {
                            Thread.sleep(500);
                        }
                    }
                } else {
                    System.out.println(String.format("WARN: class %s field %s is not a module", moduleClass.getName(), f.getName()));
                }
            }
        }
    }
}
















