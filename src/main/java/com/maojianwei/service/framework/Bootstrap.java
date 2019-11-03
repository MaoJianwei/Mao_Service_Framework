package com.maojianwei.service.framework;

import com.maojianwei.service.framework.core.MaoRunningCore;

/**
 * Hello world!
 *
 */
public class Bootstrap
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        MaoRunningCore core = MaoRunningCore.getInstance();
        core.startPool();

        synchronized (core) {
            try {
                core.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Wait exited.");
            }
        }
    }
}
















