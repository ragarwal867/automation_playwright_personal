package net.automation.utils.lazyloader;

import java.util.function.Supplier;

public class LazyLoader<T> {
    protected Supplier<T> getValueSupplier;

    public LazyLoader(Supplier<T> getValueSupplier) {
        this.getValueSupplier = getValueSupplier;
    }

    public T get() {
        return getValueSupplier.get();
    }
}
