package com.maojianwei.service.framework.lib;

public class MaoAbstractRunningTask implements Runnable{

    private MaoAbstractModule module;

    public MaoAbstractRunningTask(MaoAbstractModule module) {
        this.module = module;
    }

    @Override
    public void run() {
        System.out.println(String.format("Activating %s ...", module.name()));
        module.activate();
        System.out.println(String.format("Activated %s ...", module.name()));

//        while(!module.isNeedShutdown()) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                break; // mean that Framework goes shutdown.
//            }
//        }

        try {
            module.waitShutdown();
        } catch (InterruptedException e) {
            System.out.println(String.format("InterruptedException when module.waitShutdown() for %s", module.name()));
        }

        System.out.println(String.format("Deactivating %s ...", module.name()));
        module.deactivate();
        System.out.println(String.format("Closed %s .", module.name()));
    }
}
