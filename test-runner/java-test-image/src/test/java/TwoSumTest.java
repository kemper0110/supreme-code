import org.example.Solution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TwoSumTest {
    static Stream<Arguments> basicTests() {
        return Stream.of(
                Arguments.of(new int[]{1, 2, 3}, 4, new int[]{0, 2}),
                Arguments.of(new int[]{1234, 5678, 9012}, 14690, new int[]{1, 2}),
                Arguments.of(new int[]{2, 2, 3}, 4, new int[]{0, 1}),
                Arguments.of(new int[]{2, 3, 1}, 4, new int[]{1, 2})
        );
    }

    @ParameterizedTest
    @MethodSource
    void basicTests(int[] numbers, int target, int[] expected) {
        int[] actual = Solution.twoSum(numbers.clone(), target);
        assertNotNull(actual, "Should return an array");
        assertEquals(2, actual.length, "Returned array must be of length 2");
        assertNotEquals(actual[0], actual[1], "Indices must be distinct");
        int num1 = numbers[actual[0]];
        int num2 = numbers[actual[1]];
        assertEquals(target, num1 + num2);
    }
}
