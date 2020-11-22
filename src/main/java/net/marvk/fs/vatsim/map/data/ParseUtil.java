package net.marvk.fs.vatsim.map.data;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class ParseUtil {
    private ParseUtil() {
        throw new AssertionError("No instances of utility class " + ParseUtil.class);
    }

    public static <I, O> O parseNullSafe(final I input, final Function<I, O> mapping) {
        if (input == null) {
            return null;
        }

        return mapping.apply(input);
    }

    public static <I1, I2, O> O parseNullSafe(final I1 input1, final I2 input2, final BiFunction<I1, I2, O> mapping) {
        if (input1 == null || input2 == null) {
            return null;
        }

        return mapping.apply(input1, input2);
    }
}
