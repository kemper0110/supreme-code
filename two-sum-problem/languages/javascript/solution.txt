function twoSum(nums, target) {
    const map = new Map();
    for (let i = 0; i < nums.length; ++i) {
        const idx = map.get(nums[i]);
        if (idx != null)
            return [i, idx];
        map.set(target - nums[i], i);
    }
    return [0, 0];
}

module.exports = twoSum;