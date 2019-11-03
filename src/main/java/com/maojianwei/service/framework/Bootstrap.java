package com.maojianwei.service.framework;

import com.maojianwei.service.framework.core.MaoModuleManager;
import com.maojianwei.service.framework.core.MaoRunningCore;
import com.maojianwei.service.framework.core.web.MaoWebSystem;

/**
 * Mao service framework (lightweight).
 * @author Jianwei Mao  2019.11.03
 */
public class Bootstrap {
    public static void main(String[] args) {

        System.out.println("Mao service framework!");

        MaoRunningCore core = MaoRunningCore.getInstance();
        core.startPool();

        MaoModuleManager moduleManager = MaoModuleManager.getInstance();

        MaoWebSystem webSystem = new MaoWebSystem();
        int webKey = moduleManager.registerModule(webSystem);


        synchronized (core) {
            try {
                core.wait(); // wait for system shutdown signal.
            } catch (InterruptedException e) {
                System.out.println("InterruptedException when core.wait().");
            }
        }


        boolean webBool = moduleManager.unregisterModule(webSystem, webKey);
        webBool = moduleManager.unregisterModule(webSystem, webKey);

        core.stopPool();

        try {
            while (core.waitPoolFinish(500)) {}
            System.out.println("Running Pool finish.");
        } catch (InterruptedException e) {
            System.out.println("InterruptedException when core.waitPoolFinish().");
        }

        System.out.println("Mao service framework exited.");
    }
}
















