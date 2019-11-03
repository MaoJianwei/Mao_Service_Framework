package com.maojianwei.service.framework.lib;

public abstract class MaoAbstractModule {

    private String name;
    private boolean needShutdown = false;

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

    public void setNeedShutdown() {
        this.needShutdown = true;
    }
}
