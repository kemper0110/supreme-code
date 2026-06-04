#include <tuple>
#include <unordered_map>

std::pair<int, int> twoSum(const std::vector<int> &nums, const int target) {
    std::unordered_map<int, int> map;
    for (int i = 0; i < nums.size(); ++i) {
        const auto idx = map.find(nums[i]);
        if (idx != map.end())
            return std::pair<int, int>{i, idx->second};
        map.emplace(target - nums[i], i);
    }
    return {0, 0};
}
