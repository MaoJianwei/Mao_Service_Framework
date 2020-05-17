package com.maojianwei.service.framework;

import com.maojianwei.service.framework.core.MaoModuleManager;
import com.maojianwei.service.framework.core.MaoRunningCore;
import com.maojianwei.service.framework.incubator.network.MaoNetworkCore;
import com.maojianwei.service.framework.incubator.network.MaoNetworkUnderlay;
import com.maojianwei.service.framework.incubator.aaa.DebugAaaManager;
import com.maojianwei.service.framework.incubator.node.DebugNodeManager;
import com.maojianwei.service.framework.web.MaoWebSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mao service framework (lightweight).
 * @author Jianwei Mao  2019.11.03
 */
public class Bootstrap {
    public static void main(String[] args) {

        Logger log = LoggerFactory.getLogger("Mao Service Framework bootstrap");
        log.info("Mao service framework!");

        MaoRunningCore core = MaoRunningCore.getInstance();
        core.startPool();

        MaoModuleManager moduleManager = MaoModuleManager.getInstance();


        MaoNetworkUnderlay networkUnderlay = MaoNetworkUnderlay.getInstance();
        int networkKey = moduleManager.registerModule(networkUnderlay);

        MaoNetworkCore networkCore = MaoNetworkCore.getInstance();
        int networkCoreKey = moduleManager.registerModule(networkCore);

        DebugAaaManager debugAaaManager = DebugAaaManager.getInstance();
        int aaaManagerKey = moduleManager.registerModule(debugAaaManager);

        DebugNodeManager debugNodeManager = DebugNodeManager.getInstance();
        int nodeManagerKey = moduleManager.registerModule(debugNodeManager);

        MaoWebSystem webSystem = new MaoWebSystem();
        int webKey = moduleManager.registerModule(webSystem);


        synchronized (core) {
            try {
                core.wait(); // wait for system shutdown signal.
            } catch (InterruptedException e) {
                log.warn("InterruptedException when core.wait().");
            }
        }


        boolean webBool = moduleManager.unregisterModule(webSystem, webKey); // For demonstrating, return True
        webBool = moduleManager.unregisterModule(webSystem, webKey);         // For demonstrating, return False without affect.

        boolean nodeBool = moduleManager.unregisterModule(debugNodeManager, nodeManagerKey);
        boolean aaaBool = moduleManager.unregisterModule(debugAaaManager, aaaManagerKey);

        boolean networkCoreBool = moduleManager.unregisterModule(networkCore, networkCoreKey); // For demonstrating, return True
        boolean networkBool = moduleManager.unregisterModule(networkUnderlay, networkKey); // For demonstrating, return True


        core.stopPool();

        try {
            while (!core.waitPoolFinish(500)) {}
            log.info("Running Pool finish.");
        } catch (InterruptedException e) {
            log.warn("InterruptedException when core.waitPoolFinish().");
        }

        log.info("Mao service framework exited.");
    }
}
