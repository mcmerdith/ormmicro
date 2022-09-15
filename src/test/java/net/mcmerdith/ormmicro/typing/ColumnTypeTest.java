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

    private static int sizeOrdinal = -1;
    private static int length = 0;
    private static int digits = 1;
    private static int precision = 0;

    private static final Supplier<SqlType.Size> randomSizeSupplier = () -> SqlType.Size.values()[++sizeOrdinal % (SqlType.Size.values().length)];
    private static final Supplier<Integer> randomLengthSupplier = () -> ++length;
    private static final Supplier<Integer> randomDigitSupplier = () -> ++digits;
    private static final Function<Integer, Integer> randomDecimalSupplier = random::nextInt;
    private static final Supplier<Integer> randomPrecisionSupplier = () -> ++precision;

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
                                                        boolean varyMGTypeSize, boolean varyMGLength, boolean varyMGDigits,
                                                        boolean varyMGDecimals, boolean varyMGPrecision) {
        List<List<ColumnType>> typeGroups = new ArrayList<>();

        Supplier<SqlType.Size> size = varyMGTypeSize ? getNullSupplier()
                : randomSizeSupplier;
        Supplier<Integer> length = varyMGLength ? getNullSupplier()
                : randomLengthSupplier;
        Supplier<Integer> digits = varyMGDigits ? getNullSupplier()
                : randomDigitSupplier;
        Function<Integer, Integer> decimals = varyMGDecimals ? (i) -> null
                : randomDecimalSupplier;
        Supplier<Integer> precision = varyMGPrecision ? getNullSupplier()
                : randomPrecisionSupplier;

        for (int i = 0; i < matchingGroupSize; i++) {
            Integer d = digits.get();
            typeGroups.add(
                    typeGroup(matchingGroupSize, type, size.get(), length.get(), d, decimals.apply(d), precision.get())
            );
        }

        return typeGroups;
    }

    private static List<List<ColumnType>> integerTestGroups = new ArrayList<>();

    static {
        integerTestGroups = typeTestGroup(3, SqlType.INTEGER, false, false, true, true, true);
    }

    @Test
    public void testEquals() {

    }

    @Test
    public void testHashCode() {

    }

    public <T> void assertTestGroupEquality(List<List<T>> testGroups) {
        for (int gNumber = 0; gNumber < testGroups.size(); gNumber++);
    }
}