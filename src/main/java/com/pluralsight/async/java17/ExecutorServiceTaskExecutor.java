package com.pluralsight.async.java17;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ExecutorServiceTaskExecutor {

    public static void main(String[] args) {
        run();
    }

    private static void run() {
        Random random = new Random();

        Callable<Quotation> fetchQuoteA = () -> {
            sleep(random);
            return new Quotation("Server A", random.nextInt(40, 60));
        };
        Callable<Quotation> fetchQuoteB = () -> {
            sleep(random);
            return new Quotation("Server B", random.nextInt(40, 70));
        };
        Callable<Quotation> fetchQuoteC = () -> {
            sleep(random);
            return new Quotation("Server C", random.nextInt(40, 80));
        };

        List<Callable<Quotation>> quotes = List.of(fetchQuoteA, fetchQuoteB, fetchQuoteC);
        var executorService = Executors.newFixedThreadPool(4);

        Instant now = Instant.now();

        // This will run each async task in parallel
        List<Future<Quotation>> futures = quotes.stream().map(executorService::submit)
                .collect(Collectors.toList());

        Quotation bestQuote = futures.stream().map(i -> getQuotation(i))
                .min(Comparator.comparing(Quotation::getAmount))
                .orElseThrow();

        Duration duration = Duration.between(now, Instant.now());
        System.out.println("Best quote [sync] = Server [" +
                bestQuote.getServer() + "] Amount=" + bestQuote.getAmount() + " (" + duration.toMillis() + "ms)");
    }

    private static Quotation getQuotation(Future<Quotation> i) {
        try {
            return i.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sleep(Random random) throws InterruptedException {
        Thread.sleep(random.nextInt(80, 120));
    }

    private static class Quotation {
        private String server;
        private int amount;

        public Quotation(String server, int amount) {
            this.server = server;
            this.amount = amount;
        }

        public String getServer() {
            return server;
        }

        public int getAmount() {
            return amount;
        }
    }
}
