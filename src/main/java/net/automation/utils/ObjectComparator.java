package net.automation.utils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.automation.utils.TypeHelper.getAllGetters;
import static org.junit.Assert.fail;

public class ObjectComparator {

    /**
     * Main method to compare two objects (expected and actual).
     * If expected is null, the comparison is skipped for that field.
     * If any mismatch is found, it throws an exception using the fail method.
     *
     * @param expected the expected object.
     * @param actual   the actual object to be compared with.
     */
    public static void compareObjects(Object expected, Object actual) {
        if (expected == null) {
            // Skip comparison if the expected value is null
            return;
        }

        if (isPrimitiveOrWrapperOrString(expected)) {
            // If it's a primitive type, wrapper, or string, just use equals
            if (!Objects.equals(expected, actual)) {
                fail(". Expected: " + expected + ", but got: " + actual);
            }
        } else if (expected instanceof Collection) {
            // Handle collections (unordered comparison)
            compareCollections((Collection<?>) expected, (Collection<?>) actual);
        } else {
            // Complex object, use reflection to get all getters and compare their results
            Set<Method> getters = getAllGetters(expected.getClass());
            for (Method getter : getters) {
                Object expectedValue = TypeHelper.invoke(getter, expected);
                Object actualValue = TypeHelper.invoke(getter, actual);
                try {
                    compareObjects(expectedValue, actualValue);
                } catch (Throwable e) {
                    // On failure, we append the getter name to indicate the exact field that failed
                    fail("." + getter.getName() + "()" + e.getMessage());
                }
            }
        }
    }

    /**
     * Helper method to compare two collections.
     *
     * @param expected the expected collection.
     * @param actual   the actual collection.
     */
    private static void compareCollections(Collection<?> expected, Collection<?> actual) {
        if (expected == null || actual == null) {
            if (!Objects.equals(expected, actual)) {
                fail("Expected collection: " + expected + ", but got: " + actual);
            }
            return;
        }

        if (expected.size() != actual.size()) {
            fail("Expected collection size: " + expected.size() + ", but got: " + actual.size());
        }

        // Convert to lists to ensure we can compare by index
        List<?> expectedList = (List<?>) expected;
        List<?> actualList = (List<?>) actual;

        for (int i = 0; i < expectedList.size(); i++) {
            Object expectedElement = expectedList.get(i);
            Object actualElement = actualList.get(i);

            try {
                compareObjects(expectedElement, actualElement);
            } catch (Throwable e) {
                fail("[" + i + "]" + e.getMessage());
            }
        }
    }

    /**
     * Determines if the object is a primitive type, its wrapper, or a String.
     *
     * @param obj the object to check.
     * @return true if it's a primitive type, wrapper, or string, otherwise false.
     */
    private static boolean isPrimitiveOrWrapperOrString(Object obj) {
        return obj instanceof String ||
                obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Character;
    }
}
