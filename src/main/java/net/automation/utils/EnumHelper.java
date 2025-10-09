package net.automation.utils;

public class EnumHelper {
    public static <T extends Enum<T>> T convertToEnum(Class<T> enumType, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Input value cannot be null");
        }

        if (!enumType.isEnum()) {
            throw new IllegalArgumentException("Provided type is not an enum");
        }

        try {
            return Enum.valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant " + enumType.getSimpleName() + "." + value);
        }
    }
}
