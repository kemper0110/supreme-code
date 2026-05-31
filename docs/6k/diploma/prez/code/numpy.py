import numpy as np

arr = np.array([1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
sum = np.sum(arr[arr % 2 == 0])

print(f"Результат: {sum}")  # 30