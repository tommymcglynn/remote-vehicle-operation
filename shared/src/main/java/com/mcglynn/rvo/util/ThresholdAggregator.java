package com.mcglynn.rvo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class ThresholdAggregator<T> {
    private int threshold;
    private Consumer<List<T>> handler;
    private List<T> items;
    private Lock lock;

    public ThresholdAggregator(int threshold, Consumer<List<T>> handler) {
        this.threshold = threshold;
        this.handler = handler;
        items = new ArrayList<>();
        lock = new ReentrantLock();
    }

    public void add(T item) {
        List<T> itemsToHandle = null;
        lock.lock();
        try {
            items.add(item);
            if (items.size() >= threshold) {
                itemsToHandle = items;
                items = new ArrayList<>();
            }
        }
        finally {
            lock.unlock();
        }
        if (itemsToHandle != null) {
            handler.accept(itemsToHandle);
        }
    }

}
