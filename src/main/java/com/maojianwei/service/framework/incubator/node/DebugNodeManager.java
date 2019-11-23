package com.maojianwei.service.framework.incubator.node;

import com.maojianwei.service.framework.incubator.network.MaoNetworkCore;
import com.maojianwei.service.framework.incubator.network.lib.MaoPeerDemand;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;

public class DebugNodeManager extends MaoAbstractModule {

    @MaoReference
    private MaoNetworkCore maoNetworkCore;

    private DebugNodeManager() {
        super("DebugNodeManager");
    }
    private static DebugNodeManager singletonInstance;
    public static DebugNodeManager getInstance() {
        if (singletonInstance == null) {
            synchronized (DebugNodeManager.class) {
                if (singletonInstance == null) {
                    singletonInstance = new DebugNodeManager();
                }
            }
        }
        return singletonInstance;
    }

    @Override
    public void activate() {
        maoNetworkCore.addPeerNeeds(new MaoPeerDemand("127.0.0.1", 33));
    }

    @Override
    public void deactivate() {

    }
}
