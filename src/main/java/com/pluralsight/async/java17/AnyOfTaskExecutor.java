package com.pluralsight.async.java17;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This example will fire off multiple tasks and get the result of the first one returned; not the fastest result
 */
public class AnyOfTaskExecutor {

    public static void main(String[] args) throws InterruptedException {
        run();
    }

    public static void run() throws InterruptedException {
        List<Supplier<Weather>> tasks = buildWeatherTasks();
        List<CompletableFuture<Weather>> futures = tasks.stream().map(CompletableFuture::supplyAsync)
                .toList();
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futures.toArray(CompletableFuture[]::new));

        // Get the results when all the tasks have completed, thenAccept() will be called only when all the quotation tasks
        // have completed, join will block main thread until we have a result
        anyOf.thenAccept(System.out::println).join();
    }

    private static List<Supplier<Weather>> buildWeatherTasks() {
        Random random = new Random();
        Supplier<Weather> fetchQuoteA = () -> {
            sleep(random);
            return new Weather("Server A", "Sunny");
        };
        Supplier<Weather> fetchQuoteB = () -> {
            sleep(random);
            return new Weather("Server B", "Sunny");
        };
        Supplier<Weather> fetchQuoteC = () -> {
            sleep(random);
            return new Weather("Server C", "Sunny");
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
