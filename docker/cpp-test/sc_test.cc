#include <gtest/gtest.h>
#include "solution.hpp"

struct TestCaseItem {
    std::vector<int> nums;
    int target;
};

std::vector<TestCaseItem> items{
        {{1,    2,    3},    4},
        {{1234, 5678, 9012}, 14690},
        {{2,    2,    3},    4},
        {{2,    3,    1},    4}
};

TEST(HelloTest, BasicAssertions) {
    for(const auto& item : items) {
        const auto indices = twoSum(item.nums, item.target);
        const auto result = item.nums[indices.first] + item.nums[indices.second];
        const auto expected = item.target;
        EXPECT_EQ(expected, result);
    }
}