package net.automation.utils;

import org.junit.Assert;

import java.util.stream.Collector;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class CustomCollectors {
    /**
     * Get the first element from the collection and make sure there are no other elements
     * Throws an exception if the collection is empty or contains more than 1 element
     * @param failMessage exception message
     * @return the only element in the collection
     * @param <T> type of elements in collection
     */
    public static <T> Collector<T, ?, T> single(String failMessage) {
        return collectingAndThen(
                toList(),
                list -> {
                    if (list.size() != 1) {
                        Assert.fail("Invalid number of elements (expected 1, but was %s). Details %S".formatted(list.size(), failMessage));
                    }

                    return list.get(0);
                }
        );
    }
}