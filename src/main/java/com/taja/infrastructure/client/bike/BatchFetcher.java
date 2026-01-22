package com.taja.infrastructure.client.bike;

import java.util.List;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface BatchFetcher<T> {
    Mono<List<T>> fetch(int startIndex, int endIndex);
}
