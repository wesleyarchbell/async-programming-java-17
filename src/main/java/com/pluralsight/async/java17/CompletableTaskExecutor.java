package com.pluralsight.async.java17;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class uses the completable future to run a tasks asynchronously but block on main thread when
 *  * waiting for the results when calling the join() method on the supplier object
 */
public class CompletableTaskExecutor {

    public static void main(String[] args) {
        run();
    }

    private static void run() {
        Random random = new Random();

        // we use a supplier instead of a callable
        Supplier<Quotation> fetchQuoteA = () -> {
            sleep(random);
            return new Quotation("Server A", random.nextInt(40, 60));
        };
        Supplier<Quotation> fetchQuoteB = () -> {
            sleep(random);
            return new Quotation("Server B", random.nextInt(40, 70));
        };
        Supplier<Quotation> fetchQuoteC = () -> {
            sleep(random);
            return new Quotation("Server C", random.nextInt(40, 80));
        };

        List<Supplier<Quotation>> quotes = List.of(fetchQuoteA, fetchQuoteB, fetchQuoteC);

        Instant now = Instant.now();

        // This will run each async task in parallel
        List<CompletableFuture<Quotation>> futures = quotes.stream()
                .map(CompletableFuture::supplyAsync)
                .collect(Collectors.toList());

        // join method on CompletableFuture does not throw an exception unlike Future but blocks main thread like get()
        Quotation bestQuote = futures.stream().map(CompletableFuture::join)
                .min(Comparator.comparing(Quotation::getAmount))
                .orElseThrow();

        Duration duration = Duration.between(now, Instant.now());
        System.out.println("Best quote [sync] = Server [" +
                bestQuote.getServer() + "] Amount=" + bestQuote.getAmount() + " (" + duration.toMillis() + "ms)");
    }

    private static void sleep(Random random) {
        try {
            Thread.sleep(random.nextInt(80, 120));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException();
        }
    }
}
