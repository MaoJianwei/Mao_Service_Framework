package com.maojianwei.service.framework.core.web;

import com.maojianwei.service.framework.lib.MaoAbstractModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class MaoWebSystem extends MaoAbstractModule {

    ConfigurableApplicationContext context;

    public MaoWebSystem() {
        super("MaoWebSystem");
    }

    @Override
    public void activate() {
        context = SpringApplication.run(MaoWebSystemEntry.class);
    }

    @Override
    public void deactivate() {
        if (context != null) {
            SpringApplication.exit(context);
            context = null;
        }
    }

    @SpringBootApplication
    public static class MaoWebSystemEntry {}
}
