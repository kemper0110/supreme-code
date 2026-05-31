import numpy as np

from solution import (
    create_array_0_to_9,
    create_true_array_3x3,
    get_numpy_version,
    get_odd_numbers,
    replace_odd_numbers
)


def test_get_numpy_version():
    version = get_numpy_version()

    assert isinstance(version, str)
    assert len(version) > 0

