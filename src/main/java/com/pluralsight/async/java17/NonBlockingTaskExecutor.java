package com.pluralsight.async.java17;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class will use the thenApply method of the completion stage api to not block the main thread but instead
 * pass the result of the supplier to another function
 */
public class NonBlockingTaskExecutor {

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

        Collection<Quotation> finalQuotes = new ConcurrentLinkedDeque<>();
        List<CompletableFuture<Void>> voids = new ArrayList<>();

        // Call thenAccept() to add all the quotes to a list when they return
        futures.stream()
                .forEach(i -> {
                    CompletableFuture<Void> accept = i.thenAccept(finalQuotes::add);
                    voids.add(accept);
                });

        // This will block main thread until all tasks complete
        voids.forEach(i -> i.join());
        System.out.println("Quotations: " + finalQuotes);

        // join method on CompletableFuture does not throw an exception unlike Future but blocks main thread like get()
        Quotation bestQuote = futures.stream().map(CompletableFuture::join)
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow();

        Duration duration = Duration.between(now, Instant.now());
        System.out.println("Best quote [sync] = Server [" +
                bestQuote.server() + "] Amount=" + bestQuote.amount() + " (" + duration.toMillis() + "ms)");
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
