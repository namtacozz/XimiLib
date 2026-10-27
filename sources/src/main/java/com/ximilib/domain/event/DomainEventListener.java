package com.ximilib.domain.event;

@FunctionalInterface
public interface DomainEventListener<T> {
    void onEvent(T event);
}
