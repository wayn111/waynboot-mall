package com.test;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureTest {
    public static void main(String[] args) {
        CompletableFuture
                .supplyAsync(() -> 1)
                .thenApplyAsync(integer -> integer + 1)
                .thenApplyAsync(integer -> integer * integer)
                .thenCombineAsync(CompletableFuture.completedFuture(11), (integer, integer2) -> integer + integer2)
                .thenAcceptAsync(integer -> System.out.println(integer));
    }
}
