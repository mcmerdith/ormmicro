package net.mcmerdith.ormmicro.typing;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class ColumnTypeTest {
    private static final Random random = new Random();

    private static final Supplier<SqlType.Size> randomSizeSupplier = () -> SqlType.Size.values()[random.nextInt(SqlType.Size.values().length)];
    private static final Supplier<Integer> randomLengthSupplier = () -> random.nextInt(255) + 1;
    private static final Supplier<Integer> randomDigitSupplier = () -> random.nextInt(50) + 1;
    private static final Function<Integer, Integer> randomDecimalSupplier = (digits) -> random.nextInt(digits);
    private static final Supplier<Integer> randomPrecisionSupplier = () -> random.nextInt(50);

    private static <T> Supplier<T> getNullSupplier() {
        return () -> null;
    }

    private static List<ColumnType> typeGroup(int groupSize, SqlType type, SqlType.Size pSize, Integer pLength, Integer pDigits, Integer pDecimals, Integer pPrecision) {
        List<ColumnType> types = new ArrayList<>();

        Random r = new Random();

        for (int i = 0; i < groupSize; i++) {
            SqlType.Size size = Optional.ofNullable(pSize).orElseGet(randomSizeSupplier);
            int length = Optional.ofNullable(pLength).orElseGet(randomLengthSupplier);
            int digits = Optional.ofNullable(pDigits).orElseGet(randomDigitSupplier);
            int decimals = Optional.ofNullable(pDecimals).orElseGet(() -> randomDecimalSupplier.apply(digits));
            int precision = Optional.ofNullable(pPrecision).orElseGet(randomPrecisionSupplier);

            types.add(new ColumnType.Builder(type)
                    .setSize(size)
                    .setLength(length)
                    .setDigits(digits).setDecimals(decimals)
                    .setPrecision(precision)
                    .build());
        }

        return types;
    }

    private static List<List<ColumnType>> typeTestGroup(int matchingGroupSize, SqlType type,
                                                        boolean randomSize, boolean randomLength, boolean randomDigits,
                                                        boolean randomDecimals, boolean randomPrecision) {
        List<List<ColumnType>> typeGroups = new ArrayList<>();

        Supplier<SqlType.Size> size = randomSize ? getNullSupplier()
                : randomSizeSupplier;
        Supplier<Integer> length = randomLength ? getNullSupplier()
                : randomLengthSupplier;
        Supplier<Integer> digits = randomDigits ? getNullSupplier()
                : randomDigitSupplier;
        Function<Integer, Integer> decimals = randomDecimals ? (i) -> null
                : randomDecimalSupplier;
        Supplier<Integer> precision = randomPrecision ? getNullSupplier()
                : randomPrecisionSupplier;

        for (int i = 0; i < matchingGroupSize; i++) {
            int d = digits.get();
            typeGroups.add(
                    typeGroup(matchingGroupSize, type, size.get(), length.get(), d, decimals.apply(d), precision.get())
            );
        }

        return typeGroups;
    }

    private static List<List<ColumnType>> integerTestGroups = new ArrayList<>();

    static {
        integerTestGroups = typeTestGroup(3, 3, SqlType.INTEGER);
    }

    @Test
    public void testEquals() {

    }

    @Test
    public void testHashCode() {
        assertEquals(INT_1_1, INT_1_2);
        assertEquals(INT_1_1.hashCode(), INT_1_3.hashCode());
        assertEquals(INT_2_1.hashCode(), INT_2_2.hashCode());
        assertEquals(INT_3_1.hashCode(), INT_3_2.hashCode());
        assertNotEquals(INT_1_1.hashCode(), INT_2_1.hashCode());
        assertNotEquals(INT_1_1.hashCode(), INT_3_1.hashCode());
        assertNotEquals(INT_2_1.hashCode(), INT_3_1.hashCode());
    }

    public <T> void assertTestGroupEquality(List<List<T>> testGroups) {
        for (int testGroupNumber)
    }
}