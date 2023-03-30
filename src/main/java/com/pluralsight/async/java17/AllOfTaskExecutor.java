package com.pluralsight.async.java17;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This example will fire off multiple completable futures and get the result when all have completed
 */
public class AllOfTaskExecutor {

    public static void main(String[] args) throws InterruptedException {
        run();
    }

    public static void run() throws InterruptedException {
        List<Supplier<Quotation>> tasks = buildQuotationTasks();
        List<CompletableFuture<Quotation>> futures = tasks.stream().map(i -> CompletableFuture.supplyAsync(i))
                .collect(Collectors.toList());
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

        // Get the results when all the tasks have completed, thenApply() will be called only when all the quotation tasks
        // have completed
        Quotation bestQuote = allOf.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .min(Comparator.comparing(Quotation::amount))
                        .orElseThrow()
        ).join();

        System.out.println("Best Quotation = " + bestQuote);
    }

    private static List<Supplier<Quotation>> buildQuotationTasks() {
        Random random = new Random();
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
        return List.of(fetchQuoteA, fetchQuoteB, fetchQuoteC);
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
