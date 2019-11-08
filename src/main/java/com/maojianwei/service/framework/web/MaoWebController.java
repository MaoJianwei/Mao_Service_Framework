package com.maojianwei.service.framework.web;

import com.maojianwei.service.framework.core.MaoRunningCore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mao-service-control")
public class MaoWebController {

    @RequestMapping("/startDemo")
    public String startDemoModule() {
        return "start ok";
    }

    @RequestMapping("/stopDemo")
    public String stopDemoModule() {
        MaoRunningCore core = MaoRunningCore.getInstance();
        synchronized (core) {
            core.notify();
        }
        return "stop ok";
    }
}








