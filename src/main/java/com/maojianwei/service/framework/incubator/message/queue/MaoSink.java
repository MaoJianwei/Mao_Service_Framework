package com.maojianwei.service.framework.incubator.message.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @param <E> Class of Event
 */
public class MaoSink<E, L extends MaoAbstractListener<E>> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final int DEFAULT_RETRY_INTERVAL = 200; // ms

    private Set<L> listeners;
    private ReentrantLock listenerLock;

    private Thread dispatcher;
    private LinkedBlockingQueue<E> eventQueue;

    public MaoSink() {
        dispatcher = new Thread(new DispatchEvent());
        eventQueue = new LinkedBlockingQueue<>();

        listeners = new HashSet<>();
        listenerLock = new ReentrantLock();
    }

    public void startSink() {
        dispatcher.start();
    }

    public void pauseSink() {
        dispatcher.interrupt();
    }

    public void stopSink() {
        dispatcher.interrupt();
        eventQueue.clear();
    }


    public void addListener(L listener) {
        if (listener == null)
            return;

        listenerLock.lock();
        try {
            listeners.add(listener);
        } finally {
            listenerLock.unlock();
        }
    }

    public void removeListener(L listener) {
        if (listener == null)
            return;

        listenerLock.lock();
        try {
            listeners.remove(listener);
        } finally {
            listenerLock.unlock();
        }
    }


    public boolean postEvent(E event) {
        return postEvent(event, 0);
    }

    public boolean postEvent(E event, int retryCount) {
        return postEvent(event, retryCount, DEFAULT_RETRY_INTERVAL);
    }

    public boolean postEvent(E event, int retryCount, int waitMillis) {
        do {
            if (eventQueue.offer(event)) {
                return true;
            }
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException e) {
                return false;
            }
        } while ((retryCount--) > 0);
        return false;
    }


    private class DispatchEvent implements Runnable {
        @Override
        public void run() {
            while (true) {
                E event = null;
                try {
                    event = eventQueue.take();
                } catch (InterruptedException e) {
                    log.info("DispatchEvent take InterruptedException");
                    break;
                }
                listenerLock.lock();
                try {
                    for (L l : listeners) {
                        if (l.isRelevant()) {
                            if(!l.postEvent(event)) {
                                log.warn("MaoAbstractListener.postEvent false");
                            }
                        }
                    }
                } finally {
                    listenerLock.unlock();
                }
            }
        }
    }
}














