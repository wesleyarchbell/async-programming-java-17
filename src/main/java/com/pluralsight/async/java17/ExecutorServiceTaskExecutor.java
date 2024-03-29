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

/**
 * This class uses the executor service to run a few tasks asynchronously but block on main thread when
 * waiting for the results when calling the get() method on the future object
 */
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
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow();

        Duration duration = Duration.between(now, Instant.now());
        System.out.println("Best quote [async] = Server [" +
                bestQuote.server() + "] Amount=" + bestQuote.amount() + " (" + duration.toMillis() + "ms)");
    }

    private static Quotation getQuotation(Future<Quotation> i) {
        try {
            // this blocks the main thread
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
}
