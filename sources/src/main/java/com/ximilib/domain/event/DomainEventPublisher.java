package com.ximilib.domain.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DomainEventPublisher: Bộ điều phối Observer Pattern trong tiến trình theo ARCHITECTURE.md §5.
 */
public class DomainEventPublisher {

    private static final Map<Class<?>, List<DomainEventListener<?>>> LISTENERS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> void subscribe(Class<T> eventType, DomainEventListener<T> listener) {
        LISTENERS.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T> void unsubscribe(Class<T> eventType, DomainEventListener<T> listener) {
        List<DomainEventListener<?>> list = LISTENERS.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void publish(T event) {
        if (event == null) return;
        List<DomainEventListener<?>> list = LISTENERS.get(event.getClass());
        if (list != null) {
            for (DomainEventListener<?> listener : list) {
                ((DomainEventListener<T>) listener).onEvent(event);
            }
        }
    }
}
