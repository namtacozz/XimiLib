package com.ximilib.application.port;

import com.ximilib.domain.repository.TransactionContext;
import java.util.function.Consumer;
import java.util.function.Function;

public interface TransactionCoordinator {

    <T> T execute(Function<TransactionContext, T> action);

    void executeVoid(Consumer<TransactionContext> action);
}
