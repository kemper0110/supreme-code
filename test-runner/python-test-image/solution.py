import numpy as np


def get_numpy_version():
    return np.__version__


def create_array_0_to_9():
    return np.arange(10)


def create_true_array_3x3():
    return np.full((3, 3), True, dtype=bool)


def get_odd_numbers(arr):
    return arr[arr % 2 == 1]


def replace_odd_numbers(arr):
    result = arr.copy()
    result[result % 2 == 1] = -1
    return result
