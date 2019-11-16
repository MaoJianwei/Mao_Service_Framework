package com.maojianwei.service.framework.incubator.message.queue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class MaoAbstractListener<E> {

    private final int DEFAULT_RETRY_INTERVAL = 200; // ms

    private boolean needStop;
    private Thread eventProcessor;
    private LinkedBlockingQueue<E> eventQueue;

    private MaoAbstractListener() {
        needStop = true;
        eventProcessor = new Thread(new ProcessEvent());
        eventQueue = new LinkedBlockingQueue<>();
    }

    /**
     * May be override by child class.
     *
     * @return
     */
    protected boolean isRelevant() {
        return true;
    }

    protected abstract void process(E event);


    /**
     * This MUST NOT be blocked infinitely.
     * <p>
     * It can can be failed to post an event to queue, and this should be logged/warned.
     *
     * @param event
     */
    protected boolean postEvent(E event) {
        try {
            return eventQueue.offer(event, DEFAULT_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.out.println("WARN: MaoAbstractListener.postEvent InterruptedException");
            return false;
        }
    }


    /**
     * Only used by Sink.
     */
    private void startListener() {
        needStop = false;
        eventProcessor.start();
    }

    /**
     * Only used by Sink.
     */
    private void stopListener() {
        needStop = true;
        eventProcessor.interrupt();
    }

    private class ProcessEvent implements Runnable {
        @Override
        public void run() {
            while (!needStop) {
                E event;
                try {
                    event = eventQueue.take();
                } catch (InterruptedException e) {
                    System.out.println("INFO: ProcessEvent interrupt, exit.");
                    break;
                }

                try {
                    if (!needStop) {
                        process(event);
                    } else {
                        System.out.println("INFO: ProcessEvent need stop before process, exit.");
                        break;
                    }
                } catch (Exception e) {
                    // Be tolerant for exceptions thrown by modules' listener.
                    System.out.println(String.format("WARN: Module listener throw Exception %s", e.getMessage()));
                }
            }
        }
    }
}



















