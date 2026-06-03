const twoSum = require('./solution');

const items = [
    [[1, 2, 3], 4,],
    [[1234, 5678, 9012], 14690,],
    [[2, 2, 3], 4,],
    [[2, 3, 1], 4,],
]

test('test on 4 cases', () => {
    for (const item of items) {
        const [nums, target] = item

        const result = twoSum(nums, target);
        const [n1, n2] = [nums[result[0]], nums[result[1]]]
        expect(n1 + n2).toBe(target);
    }
})