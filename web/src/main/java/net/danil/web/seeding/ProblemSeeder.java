package net.danil.web.seeding;

import lombok.RequiredArgsConstructor;
import net.danil.web.model.Language;
import net.danil.web.model.Problem;
import net.danil.web.model.ProblemLanguage;
import net.danil.web.repository.ProblemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProblemSeeder implements CommandLineRunner {
    private final ProblemRepository problemRepository;
    Logger logger = LoggerFactory.getLogger(ProblemSeeder.class);

    @Override
    public void run(String... args) throws Exception {
        if (problemRepository.count() > 0) return;

        var saved = problemRepository.save(
                Problem.builder()
                        .id(1L)
                        .name("Two sum")
                        .active(true)
                        .difficulty(Problem.Difficulty.Easy)
                        .description(description)
                        .build()
        );

        if (saved == null) {
            logger.warn("two sum problem not created");
            return;
        }
        saved.setLanguages(List.of(
                ProblemLanguage.builder()
                        .problem(saved)
                        .id(1L)
                        .language(Language.Java)
                        .template(template)
                        .test(test)
                        .build()
        ));
        saved = problemRepository.save(saved);
        if (saved != null) {
            logger.info("created two sum problem {}", saved);
        } else {
            logger.warn("two sum problem not created");
        }
    }


    final private String test = """
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
            """;

    final private String description = """
            <div class="elfjS" data-track-load="description_content"><p>Given an array of integers <code>nums</code>&nbsp;and an integer <code>target</code>, return <em>indices of the two numbers such that they add up to <code>target</code></em>.</p>

                        <p>You may assume that each input would have <strong><em>exactly</em> one solution</strong>, and you may not use the <em>same</em> element twice.</p>

                        <p>You can return the answer in any order.</p>

                        <p>&nbsp;</p>
                        <p><strong class="example">Example 1:</strong></p>

                        <pre><strong>Input:</strong> nums = [2,7,11,15], target = 9
                        <strong>Output:</strong> [0,1]
                        <strong>Explanation:</strong> Because nums[0] + nums[1] == 9, we return [0, 1].
                        </pre>

                        <p><strong class="example">Example 2:</strong></p>

                        <pre><strong>Input:</strong> nums = [3,2,4], target = 6
                        <strong>Output:</strong> [1,2]
                        </pre>

                        <p><strong class="example">Example 3:</strong></p>

                        <pre><strong>Input:</strong> nums = [3,3], target = 6
                        <strong>Output:</strong> [0,1]
                        </pre>

                        <p>&nbsp;</p>
                        <p><strong>Constraints:</strong></p>

                        <ul>
                        \t<li><code>2 &lt;= nums.length &lt;= 10<sup>4</sup></code></li>
                        \t<li><code>-10<sup>9</sup> &lt;= nums[i] &lt;= 10<sup>9</sup></code></li>
                        \t<li><code>-10<sup>9</sup> &lt;= target &lt;= 10<sup>9</sup></code></li>
                        \t<li><strong>Only one valid answer exists.</strong></li>
                        </ul>

                        <p>&nbsp;</p>
                        <strong>Follow-up:&nbsp;</strong>Can you come up with an algorithm that is less than <code>O(n<sup>2</sup>)</code><font face="monospace">&nbsp;</font>time complexity?</div>
            """;

    final private String template = """
            package org.example;

            import java.util.HashMap;
                                                            
            public class Solution {
                public static int[] twoSum(int[] nums, int target) {
                    final var map = new HashMap<Integer, Integer>();
                    for(int i = 0; i < nums.length; ++i) {
                        final var idx = map.get(nums[i]);
                        if(idx != null)
                            return new int[]{i, idx};
                        map.put(target - nums[i], i);
                    }
                    return new int[]{};
                }
            }
            """;

}
