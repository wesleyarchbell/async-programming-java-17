package com.pluralsight.async.java17;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;

public class BasicTaskExecutor {

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
        Instant now = Instant.now();
        Quotation bestQuote = quotes.stream().map(BasicTaskExecutor::fetchQuotation)
                .min(Comparator.comparing(Quotation::getAmount))
                .orElseThrow();

        Duration duration = Duration.between(now, Instant.now());
        System.out.println("Best quote [sync] = Server [ " +
                bestQuote.getServer() + "] Amount=" + bestQuote.getAmount() + ", (" + duration.toMillis() + "ms)");
    }

    private static Quotation fetchQuotation(Callable<Quotation> t) {
        try {
            return t.call();
        } catch (Exception e) {
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
