---
name: supreme-code-problem
description: Use this skill working with problems at SupremeCode
---

Ты занимаешься редактирование задачи на платформе по решению алгоритмических задач SupremeCode.

# Структура задачи

Задача имеет следующую структуру:
- `description.md` - описание задачи
- `info.json` - метаданные о задаче
- `languages` - директория с реализациями задачи на языках
    - `{languageId}` - директория с именем идентификатором языка, который должен соответствовать platform-info
        - `test.txt` - автотесты для проверки решения пользователя
        - `solution.txt` - эталонное решение задачи
        - `solutionTemplate.txt` - шаблон решения задачи, отображаемый пользователю

Если файла или папки нет, то ты можешь создать их сам.
Проверяй структуру проекта по указанной выше структуре.

JSON Schema для `info.json`:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["name", "tags"],
  "additionalProperties": false,
  "properties": {
    "name": {
      "type": "string",
      "description": "Название задачи"
    },
    "tags": {
      "type": "array",
      "description": "Массив тегов задачи, каждый элемент должен соответствовать platform-info",
      "items": {
        "type": "string"
      },
      "minItems": 1
    }
  }
}
```

Файл `test.txt` содержит автотесты на определенном тестовом фреймворке для языка.
Тесты в этом файле проверяют функции из `solution.txt`.

Файл `solutionTemplate.txt` - это часть файла `solution.txt`, из которого вырезали само решение,
но оставили сигнатуру функции.

Файл `solution.txt` содержит эталонное решение задачи. Он будет использован для проверки валидности задачи.

Эти 3 файла должны быть синхронизированы между собой. Если ты вносишь изменения в один из них, то тебе нужно внести соответствующие изменения в другие файлы.

# Формат задачи

Если задача состоит из нескольких заданий, то выделяй каждое задание в отдельную функцию в решении.

Пользователь должен точно понимать, какой формат ввода ему ожидать, и какой формат вывода ожидает система тестирования.
Это можно сделать типизацией языка, комментариями, описанием в `description.md` или любым другим способом, который подходит для данного языка.

# Особенности реализации языков

## Java

Используется junit.jupiter.

```java
import org.example.Solution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class JunitTest {
    static Stream<Arguments> basicTests() {
        // ...
    }
    @ParameterizedTest
    @MethodSource
    void basicTests() {
        // ...
    }
}
```

## C++

Используется gtest.

```cpp
#include <gtest/gtest.h>
#include "solution.hpp"

TEST(HelloTest, BasicAssertions) {
    // ...
}
```

## JavaScript

Используется jest.

```javascript
const solution = require('./solution');

test('test-case', () => {
    // ...
})
```

## Python

Используется pytest.

```python
from solution import *

def test_case1():
    assert 1 == 1
```

# Использование SupremeCode MCP

MCP работает с текущей директорией.
Используй tool `importThisProblem`, чтобы MCP получил новое содержимое файлов.
Используй tool `validate`, чтобы проверить задачу через MCP.
Tools `validate` уже выполняет принудительный `import`.

Если пользователь сказал, что изменил задачу извне, то выполни tool `exportThisProblem`,
чтобы получить новое содержимое файлов от MCP.

Если запрос пользователя касается создания новой задачи, то предварительно спроси,
можно ли использовать примеры других задач.
Если пользователь разрешил, то используй `listProblems`, чтобы получить список его задач.
Затем `exportProblem`, чтобы загрузить задачу как пример.

Если запрос пользователя касается доработки текущей задачи, то используй реализации в папке `languages`.

# Platform-info

Это объект с актуальной информацией о платформе. Предоставляется через SupremeCode MCP.
Содержит актуальный набор тегов и языков, которые поддерживает платформа.

# WorkFlow

1. Получи актуальную информацию о платформе, используя tool `platform-info`.
2. Изучи задачу в текущей директории.
3. Выполни пожелание пользователя. 
