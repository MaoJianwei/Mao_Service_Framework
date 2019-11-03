package com.maojianwei.service.framework.lib;

public abstract class MaoAbstractModule {

    private Boolean needShutdown = false;

    private String name;
    public MaoAbstractModule(String name) {
        this.name = name;
    }


    public abstract void activate();

    public abstract void deactivate();


    public String name() {
        return name;
    }

    public boolean isNeedShutdown() {
        return needShutdown;
    }

    public void waitShutdown() throws InterruptedException {
        synchronized (needShutdown) {
            needShutdown.wait();
        }
    }

    public void setNeedShutdown() {
        synchronized (needShutdown) {
            needShutdown.notify();
        }
        needShutdown = true; // needShutdown will be replaced by another Boolean object(true)
                             // so, must notify() first.
    }
}













