package net.automation.utils.lazyloader;

import java.util.function.Supplier;

public class OneTimeLazyLoader<T> extends LazyLoader<T>{
    private boolean isLoaded;

    private T value;

    public OneTimeLazyLoader(Supplier<T> getValueSupplier) {
        super(getValueSupplier);
    }

    @Override
    public synchronized T get() {
        if (!isLoaded) {
            value = getValueSupplier.get();
            isLoaded = true;
        }

        return value;
    }
}
