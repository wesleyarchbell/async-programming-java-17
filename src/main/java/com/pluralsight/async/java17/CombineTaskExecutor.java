package com.pluralsight.async.java17;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This example will take the result of two separate async results and combine then together once they have all completed
 */
public class CombineTaskExecutor {

    record TravelPage(Quotation quotation, Weather weather) {}

    public static void main(String[] args) {
        run();
    }

    private static void run() {

        List<Supplier<Quotation>> quotationTasks = buildQuotationTasks();
        List<Supplier<Weather>> weatherTasks = buildWeatherTasks();

        // Run the quotations tasks as completable futures
        List<CompletableFuture<Quotation>> quoteFutures = quotationTasks.stream()
                .map(CompletableFuture::supplyAsync).collect(Collectors.toList());

        // Run the weather tasks as computable futures
        List<CompletableFuture<Weather>> weatherFutures = weatherTasks.stream()
                .map(CompletableFuture::supplyAsync).collect(Collectors.toList());

        // Return a computable future when all the quotation tasks are run
        CompletableFuture<Void> allOfQuotations = CompletableFuture.allOf(quoteFutures.toArray(CompletableFuture[]::new));

        // Return a computable future when one of the weather tasks are done
        CompletableFuture<Weather> weather = CompletableFuture.anyOf(weatherFutures.toArray(CompletableFuture[]::new))
                .thenApply(w -> (Weather)w);

        // Get the best quote when available
        CompletableFuture<Quotation> bestQuote = allOfQuotations.thenApply(v ->
                quoteFutures.stream()
                        .map(CompletableFuture::join)
                        .min(Comparator.comparing(Quotation::amount))
                        .orElseThrow()
        );

        // Chain both futures to get a result to produce the travel page (this is the better method over combination)
        CompletableFuture<Void> done = bestQuote.thenCompose(q ->
                weather.thenApply(w -> new TravelPage(q, w))
                        .thenAccept(p -> System.out.println("thenCompose: " + p)));

        // block main thread until result is complete
        done.join();

        // You can also use and Combine to combine both futures
        CompletableFuture<Void> done2 = bestQuote.thenCombine(weather, TravelPage::new)
                .thenAccept(p -> System.out.println("thenCombine: " + p));

        // block main thread until result is complete
        done2.join();
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
