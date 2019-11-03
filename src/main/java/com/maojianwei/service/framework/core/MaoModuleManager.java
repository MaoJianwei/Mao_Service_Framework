package com.maojianwei.service.framework.core;


import com.maojianwei.service.framework.lib.MaoAbstractModule;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MaoModuleManager {

    private static int REGISTER_FAIL = -1;

    private Random randomGenerator = new Random();
    private Map<Integer, MaoAbstractModule> registeredModules = new HashMap<>();
    private Map<MaoAbstractModule, Integer> registeredModulesReverse = new HashMap<>();

    private MaoRunningCore runningCore;

    public MaoModuleManager() {
        runningCore = MaoRunningCore.getInstance();
    }

    private static MaoModuleManager singletonInstance;
    public static MaoModuleManager getInstance() {
        if (singletonInstance == null) {
            synchronized (MaoModuleManager.class) {
                if (singletonInstance == null) {
                    singletonInstance = new MaoModuleManager();
                }
            }
        }
        return singletonInstance;
    }

    /**
     * @param module
     * @return authentication key for unregister.
     */
    public int registerModule(MaoAbstractModule module) {

        if (registeredModules.containsValue(module)) {
            if (registeredModulesReverse.containsKey(module)) {
                return registeredModulesReverse.get(module);
            } else {
                return REGISTER_FAIL;
            }
        }

        int authKey;
        for (int i = 0; i < 3; i++) {
            authKey = module.hashCode() * randomGenerator.nextInt();
            if (REGISTER_FAIL != authKey && !registeredModules.containsKey(authKey)) {
                registeredModules.put(authKey, module);
                registeredModulesReverse.put(module, authKey);

                runningCore.runModule(module);

                return authKey;
            }
        }
        return REGISTER_FAIL;
    }

    public boolean unregisterModule(MaoAbstractModule module, int authKey) {
        MaoAbstractModule old = registeredModules.get(authKey);
        if (old != null && old == module) {

            module.setNeedShutdown();

            registeredModules.remove(authKey);
            registeredModulesReverse.remove(module);
            return true;
        } else {
            return false;
        }
    }
}







