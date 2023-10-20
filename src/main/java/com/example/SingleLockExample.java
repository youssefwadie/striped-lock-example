package com.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SingleLockExample {
    
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
        final PayloadBuilder payloadBuilder = new PayloadBuilder();
        final Lock singleLock = new ReentrantLock();

        final AtomicInteger numberOfFailures = new AtomicInteger(0);
        final ExecutorService executorService = Executors.newFixedThreadPool(100);



        for (int i = 1; i < 1001; i++) {
            final int threadNumber = i;
            executorService.submit(() -> {
                try {
                    singleLock.lock();
                    try {
                        Payload payload = payloadBuilder.type(String.valueOf(threadNumber)).build();
                        payloadBuilder.clear();
                        System.out.println(payload);
                    } catch (IllegalArgumentException ex) {
                        numberOfFailures.incrementAndGet();
                    }
                } finally {
                    singleLock.unlock();
                }
            });
        }

        // this should make sure all threads have finished execution
        Thread.sleep(5000);

        System.err.println("builder failures: " + numberOfFailures.get());
    }
}
