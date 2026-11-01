package com.ximilib.application.port;

import com.ximilib.domain.repository.TransactionContext;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * TransactionCoordinator: Cổng Application Port cho phép Application Service mở và điều phối Transaction Boundary.
 * Application hoàn toàn KHÔNG import java.sql hay infrastructure (R-01).
 */
public interface TransactionCoordinator {

    <T> T execute(Function<TransactionContext, T> action);

    void executeVoid(Consumer<TransactionContext> action);
}
