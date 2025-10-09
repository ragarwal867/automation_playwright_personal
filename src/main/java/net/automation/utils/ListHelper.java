package net.automation.utils;

import java.util.List;
import java.util.stream.Collectors;

public class ListHelper {
    public static <T> List<T> removeElementByValue(List<T> list, T value) {
        return list.stream()
                .filter(element -> !element.equals(value))
                .collect(Collectors.toList());
    }
}
