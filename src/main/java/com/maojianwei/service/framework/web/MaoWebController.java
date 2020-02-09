package com.maojianwei.service.framework.web;

import com.maojianwei.service.framework.core.MaoRunningCore;
import com.maojianwei.service.framework.incubator.network.MaoNetworkCore;
import com.maojianwei.service.framework.incubator.network.lib.MaoPeerDemand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

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

    @GetMapping(value = "/addPeer/{peerIp}/{peerPort}", produces = {APPLICATION_JSON_UTF8_VALUE})
    public String addPeer(@PathVariable("peerIp") String peerIp, @PathVariable("peerPort") Integer peerPort) {
        MaoNetworkCore networkCore = MaoNetworkCore.getInstance();
        networkCore.addPeerNeeds(new MaoPeerDemand(peerIp, peerPort));
        return "add " + peerIp + ":" + peerPort;
    }
}








