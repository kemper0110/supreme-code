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