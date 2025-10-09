package net.automation.utils;

import lombok.Getter;
import lombok.Setter;

public class Wrapper<T> {
    @Getter @Setter
    private T value;
}
