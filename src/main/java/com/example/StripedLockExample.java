package com.example;

import com.google.common.util.concurrent.Striped;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class StripedLockExample {

    private static class Payload {
        private final String type;

        private Payload(String type) {
            if (type == null) {
                throw new IllegalArgumentException("Type cannot be null");
            }
            this.type = type;
        }

        @Override
        public String toString() {
            return "Payload{" + "type='" + type + "'}";
        }
    }

    private static class PayloadBuilder {
        private String type;

        private PayloadBuilder type(String type) {
            this.type = type;
            return this;
        }

        public Payload build() {
            return new Payload(type);
        }

        public void clear() {
            this.type = null;
        }
    }


    public static void main(String[] args) throws InterruptedException {
        final Striped<Lock> stripedLock = Striped.lock(1001);
        final PayloadBuilder payloadBuilder = new PayloadBuilder();
        final AtomicInteger numberOfFailures = new AtomicInteger(0);
        final ExecutorService executorService = Executors.newFixedThreadPool(100);

        for (int i = 1; i < 1001; i++) {
            final int userId = i;
            executorService.submit(() -> {
                final Lock lock = stripedLock.get(userId);
                try {
                    lock.lock();
                    try {
                        Payload payload = payloadBuilder.type(String.valueOf(userId)).build();
                        payloadBuilder.clear();
                        System.out.println(payload);
                    } catch (IllegalArgumentException ex) {
                        numberOfFailures.incrementAndGet();
                    }
                } finally {
                    lock.unlock();
                }

            });
        }

        // this should make sure all threads have finished execution
        Thread.sleep(5000);

        System.err.println("builder failures: " + numberOfFailures.get());
    }
}
