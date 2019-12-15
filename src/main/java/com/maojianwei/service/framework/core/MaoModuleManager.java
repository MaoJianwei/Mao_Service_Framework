package com.maojianwei.service.framework.core;


import com.maojianwei.service.framework.lib.MaoAbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MaoModuleManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static int REGISTER_FAIL = -1;

    private Random randomGenerator = new Random();
    private Map<Integer, MaoAbstractModule> registeredModules = new HashMap<>();
    private Map<MaoAbstractModule, Integer> registeredModulesReverse = new HashMap<>();

    private MaoRunningCore runningCore;


    private MaoModuleManager() {
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


    public MaoAbstractModule getModule(Class<? extends MaoAbstractModule> moduleClass) {
        for (int i = 0; i < 3; i++) {
            // need Lock to iterate the set of modules.
            // but here, we use retry to deal with the related exception.
            try {
                for (MaoAbstractModule module : registeredModules.values()) {
                    if (module.getClass() == moduleClass) {
                        return module;
                    }
                }
            } catch (Exception e) {
                log.warn("Fail to iterate, @getModule, retry ...");
            }
        }
        return null;
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







